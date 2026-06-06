<template>
  <div class="grading-detail-container">
    <div v-loading="loading" class="content-wrapper">
      <template v-if="submission">
        <div class="paper-header card">
          <div class="header-top">
            <h1>{{ submission.paperTitle }}</h1>
            <div>
              <el-tag :type="getStatusTag(submission.status)" size="large">
                {{ getStatusText(submission.status) }}
              </el-tag>
            </div>
          </div>

          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="学生姓名">{{ submission.studentName }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ submission.className }}</el-descriptions-item>
            <el-descriptions-item label="第几次作答">第 {{ submission.attemptNumber }} 次</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ formatDate(submission.submitTime) }}</el-descriptions-item>
            <el-descriptions-item label="自动判分">
              <span class="score">{{ submission.autoScore || 0 }} 分</span>
            </el-descriptions-item>
            <el-descriptions-item label="人工判分">
              <span class="score">{{ submission.manualScore || 0 }} 分</span>
            </el-descriptions-item>
            <el-descriptions-item label="试卷总分">{{ submission.paperTotalScore }} 分</el-descriptions-item>
            <el-descriptions-item label="最终得分" :span="2">
              <span class="total-score">{{ submission.totalScore || 0 }} 分</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div
          v-for="(answer, idx) in submission.answers"
          :key="answer.id"
          class="answer-card card"
        >
          <div class="answer-header">
            <div class="question-title">
              <span class="q-num">第 {{ idx + 1 }} 题</span>
              <el-tag :type="getQuestionTypeTag(answer.question?.questionType)" size="small">
                {{ getQuestionTypeText(answer.question?.questionType) }}
              </el-tag>
              <span class="q-score">（{{ answer.question?.score }} 分）</span>
              <span class="q-text">{{ answer.question?.title }}</span>
            </div>
            <div class="score-display">
              <template v-if="answer.question?.questionType !== 'PROGRAMMING'">
                <el-tag
                  :type="answer.score === answer.question?.score ? 'success' : 'danger'"
                  size="large"
                >
                  得分: {{ answer.score || 0 }} / {{ answer.question?.score }}
                </el-tag>
              </template>
              <template v-else>
                <el-tag :type="answer.isManualGraded ? 'success' : 'warning'" size="large">
                  {{ answer.isManualGraded ? `得分: ${answer.score || 0}` : '待批改' }}
                </el-tag>
              </template>
            </div>
          </div>

          <div class="question-body">
            <div class="section">
              <div class="section-title">题目描述</div>
              <div class="description">{{ answer.question?.description }}</div>
            </div>

            <template v-if="answer.question?.questionType === 'PROGRAMMING'">
              <div v-if="answer.question?.requirements" class="section">
                <div class="section-title">要求说明</div>
                <pre class="requirements">{{ answer.question.requirements }}</pre>
              </div>
              <el-row :gutter="20" v-if="answer.question?.sampleInput || answer.question?.sampleOutput">
                <el-col :xs="24" :sm="12" v-if="answer.question?.sampleInput">
                  <div class="section">
                    <div class="section-title">示例输入</div>
                    <pre class="code-block">{{ answer.question.sampleInput }}</pre>
                  </div>
                </el-col>
                <el-col :xs="24" :sm="12" v-if="answer.question?.sampleOutput">
                  <div class="section">
                    <div class="section-title">示例输出</div>
                    <pre class="code-block">{{ answer.question.sampleOutput }}</pre>
                  </div>
                </el-col>
              </el-row>

              <div class="section">
                <div class="section-title">学生提交的代码</div>
                <pre class="code-block student-code">{{ answer.answerCode || '（未作答）' }}</pre>
              </div>

              <div class="section">
                <div class="section-title">参考答案</div>
                <pre class="code-block ref-answer">{{ answer.question?.referenceAnswer || '（无）' }}</pre>
              </div>

              <div v-if="isPending" class="grading-section">
                <div class="section-title">
                  打分
                  <span class="score-max">（满分 {{ answer.question?.score }} 分）</span>
                </div>
                <div class="grade-input-row">
                  <el-input-number
                    v-model="gradeMap[answer.id].score"
                    :min="0"
                    :max="answer.question?.score || 100"
                    size="large"
                  />
                </div>
                <div class="section-title" style="margin-top: 12px">评语（可选）</div>
                <el-input
                  v-model="gradeMap[answer.id].feedback"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入评语"
                />
              </div>
            </template>

            <template v-else>
              <div v-if="parsedOptions(answer.question)" class="section">
                <div class="section-title">选项</div>
                <div class="options-list">
                  <div
                    v-for="(opt, optIdx) in parsedOptions(answer.question)"
                    :key="optIdx"
                    :class="['option-item', getOptionClass(answer, opt, optIdx)]"
                  >
                    <span class="opt-key">{{ getOptionKey(opt, optIdx) }}.</span>
                    <span class="opt-text">{{ opt }}</span>
                    <el-icon v-if="isCorrectOption(answer, opt, optIdx)" class="icon-correct">
                      <CircleCheck />
                    </el-icon>
                    <el-icon v-else-if="isWrongSelected(answer, opt, optIdx)" class="icon-wrong">
                      <CircleClose />
                    </el-icon>
                  </div>
                </div>
              </div>

              <el-descriptions :column="2" border size="small" style="margin-top: 12px">
                <el-descriptions-item label="学生答案">
                  <el-tag type="primary">{{ answer.selectedAnswer || '（未作答）' }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="标准答案">
                  <el-tag type="success">{{ answer.question?.correctAnswer || '（无）' }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <div v-if="answer.question?.answerExplanation" class="section">
                <div class="section-title">答案解析</div>
                <div class="explanation">{{ answer.question.answerExplanation }}</div>
              </div>
            </template>

            <div v-if="!isPending && answer.feedback" class="section">
              <div class="section-title">教师评语</div>
              <div class="feedback">{{ answer.feedback }}</div>
            </div>
          </div>
        </div>

        <div v-if="isPending" class="submit-actions card">
          <el-button @click="$router.back()">返回</el-button>
          <el-button type="primary" :loading="submitting" @click="handleGrade">
            完成批改
          </el-button>
        </div>
        <div v-else class="submit-actions card">
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSubmissionDetail, gradeExamSubmission } from '@/api/teacherExam'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const submission = ref(null)
const gradeMap = reactive({})

const isPending = computed(() => submission.value?.status === 'PENDING_GRADING')

onMounted(() => {
  loadDetail()
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await getSubmissionDetail(route.params.id)
    if (res.code === 200) {
      submission.value = res.data
      for (const a of res.data.answers || []) {
        if (a.question?.questionType === 'PROGRAMMING') {
          gradeMap[a.id] = { score: a.score || 0, feedback: a.feedback || '' }
        }
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

async function handleGrade() {
  await ElMessageBox.confirm('确定完成批改吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })

  submitting.value = true
  try {
    const answers = []
    for (const key in gradeMap) {
      answers.push({
        answerId: parseInt(key),
        score: gradeMap[key].score,
        feedback: gradeMap[key].feedback
      })
    }
    const res = await gradeExamSubmission(route.params.id, { answers })
    if (res.code === 200) {
      ElMessage.success('批改完成')
      submission.value = res.data
    } else {
      ElMessage.error(res.message || '批改失败')
    }
  } catch (e) {}
  finally {
    submitting.value = false
  }
}

function parsedOptions(question) {
  if (!question?.options) return []
  try { return JSON.parse(question.options) } catch (e) { return [] }
}

function getOptionKey(opt, idx) {
  const m = opt.match(/^([A-Z])\./)
  return m ? m[1] : String.fromCharCode(65 + idx)
}

function isCorrectOption(answer, opt, idx) {
  if (!answer.question?.correctAnswer) return false
  const key = getOptionKey(opt, idx)
  return answer.question.correctAnswer.split(',').map(s => s.trim()).includes(key)
}

function isWrongSelected(answer, opt, idx) {
  if (!answer.selectedAnswer) return false
  const key = getOptionKey(opt, idx)
  const selected = answer.selectedAnswer.split(',').map(s => s.trim())
  const correct = answer.question?.correctAnswer?.split(',').map(s => s.trim()) || []
  return selected.includes(key) && !correct.includes(key)
}

function getOptionClass(answer, opt, idx) {
  const classes = []
  if (isCorrectOption(answer, opt, idx)) classes.push('correct')
  if (isWrongSelected(answer, opt, idx)) classes.push('wrong')
  return classes
}

function getQuestionTypeText(type) {
  const map = { PROGRAMMING: '编程题', SINGLE_CHOICE: '单选题', MULTIPLE_CHOICE: '多选题' }
  return map[type] || '编程题'
}

function getQuestionTypeTag(type) {
  const map = { PROGRAMMING: '', SINGLE_CHOICE: 'warning', MULTIPLE_CHOICE: 'success' }
  return map[type] || ''
}

function getStatusText(status) {
  const map = { DRAFT: '草稿', SUBMITTED: '已提交', PENDING_GRADING: '待批改', GRADED: '已批改' }
  return map[status] || '未知'
}

function getStatusTag(status) {
  const map = { DRAFT: 'info', SUBMITTED: 'primary', PENDING_GRADING: 'warning', GRADED: 'success' }
  return map[status] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.grading-detail-container {
  max-width: 1100px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.paper-header {
  .header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    h1 { margin: 0; font-size: 22px; color: #303133; }
  }
  .score { color: #409eff; font-weight: 600; }
  .total-score {
    color: #f56c6c;
    font-size: 22px;
    font-weight: 700;
  }
}

.answer-card {
  .answer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;
    .question-title {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      .q-num {
        font-size: 16px;
        font-weight: 700;
        color: #409eff;
      }
      .q-score { color: #909399; font-size: 13px; }
      .q-text { font-size: 15px; font-weight: 600; color: #303133; }
    }
  }
  .question-body {
    .section { margin-bottom: 16px; }
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 6px;
      .score-max { font-weight: normal; font-size: 12px; color: #909399; }
    }
    .description {
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
    }
    .requirements {
      background: #f5f7fa;
      padding: 12px;
      border-radius: 6px;
      white-space: pre-wrap;
      color: #606266;
      margin: 0;
      font-family: inherit;
    }
    .code-block {
      background: #1e1e1e;
      color: #d4d4d4;
      padding: 14px;
      border-radius: 6px;
      font-family: Consolas, Monaco, monospace;
      font-size: 13px;
      line-height: 1.6;
      overflow-x: auto;
      white-space: pre-wrap;
      margin: 0;
      max-height: 400px;
      overflow-y: auto;
    }
    .student-code { border-left: 4px solid #409eff; }
    .ref-answer { border-left: 4px solid #67c23a; }
    .options-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
      .option-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 14px;
        background: #f5f7fa;
        border-radius: 6px;
        border: 1px solid transparent;
        .opt-key { font-weight: 600; color: #303133; width: 24px; }
        .opt-text { flex: 1; color: #303133; }
        .icon-correct { color: #67c23a; font-size: 18px; }
        .icon-wrong { color: #f56c6c; font-size: 18px; }
        &.correct { background: #f0f9eb; border-color: #c2e7b0; }
        &.wrong { background: #fef0f0; border-color: #fbc4c4; }
      }
    }
    .explanation {
      background: #ecf5ff;
      padding: 12px;
      border-radius: 6px;
      color: #606266;
      line-height: 1.8;
      border-left: 4px solid #67c23a;
      white-space: pre-wrap;
    }
    .feedback {
      background: #fdf6ec;
      padding: 12px;
      border-radius: 6px;
      color: #606266;
      line-height: 1.8;
      border-left: 4px solid #e6a23c;
      white-space: pre-wrap;
    }
  }
  .grading-section {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    margin-top: 16px;
    .grade-input-row {
      margin-top: 8px;
    }
  }
}

.submit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
