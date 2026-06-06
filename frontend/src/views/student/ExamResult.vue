<template>
  <div class="exam-result-container">
    <div v-loading="loading" class="content-wrapper">
      <template v-if="submission">
        <div class="result-header card">
          <div class="header-top">
            <h1>{{ submission.paperTitle }}</h1>
            <el-tag :type="getStatusTag(submission.status)" size="large">
              {{ getStatusText(submission.status) }}
            </el-tag>
          </div>

          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="作答次数">第 {{ submission.attemptNumber }} 次</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ formatDate(submission.submitTime) }}</el-descriptions-item>
            <el-descriptions-item label="批改时间">{{ formatDate(submission.gradedAt) }}</el-descriptions-item>
            <el-descriptions-item label="试卷总分">{{ submission.paperTotalScore }} 分</el-descriptions-item>
            <el-descriptions-item label="自动得分" v-if="submission.status === 'GRADED' || submission.status === 'PENDING_GRADING'">
              <span class="score auto">{{ submission.autoScore || 0 }} 分</span>
            </el-descriptions-item>
            <el-descriptions-item label="人工得分" v-if="submission.status === 'GRADED'">
              <span class="score manual">{{ submission.manualScore || 0 }} 分</span>
            </el-descriptions-item>
            <el-descriptions-item label="最终得分" :span="3" v-if="submission.status === 'GRADED'">
              <span class="total-score">{{ submission.totalScore || 0 }} / {{ submission.paperTotalScore }} 分</span>
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
              <el-tag
                :type="getScoreTagType(answer)"
                size="large"
              >
                得分: {{ answer.score || 0 }} / {{ answer.question?.score }}
              </el-tag>
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

              <div class="section">
                <div class="section-title">我的代码</div>
                <pre class="code-block student-code">{{ answer.answerCode || '（未作答）' }}</pre>
              </div>

              <div v-if="showAnswer(answer.question)" class="section">
                <div class="section-title">参考答案</div>
                <pre class="code-block ref-answer">{{ answer.question?.referenceAnswer || '（无）' }}</pre>
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
                    <el-icon v-if="showAnswer(answer.question) && isCorrectOption(answer, opt, optIdx)" class="icon-correct">
                      <CircleCheck />
                    </el-icon>
                    <el-icon v-else-if="isWrongSelected(answer, opt, optIdx)" class="icon-wrong">
                      <CircleClose />
                    </el-icon>
                  </div>
                </div>
              </div>

              <el-descriptions :column="2" border size="small" style="margin-top: 12px">
                <el-descriptions-item label="我的答案">
                  <el-tag type="primary">{{ answer.selectedAnswer || '（未作答）' }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="标准答案" v-if="showAnswer(answer.question)">
                  <el-tag type="success">{{ answer.question?.correctAnswer || '（无）' }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <div v-if="showAnswer(answer.question) && answer.question?.answerExplanation" class="section">
                <div class="section-title">答案解析</div>
                <div class="explanation">{{ answer.question.answerExplanation }}</div>
              </div>
            </template>

            <div v-if="answer.feedback" class="section">
              <div class="section-title">教师评语</div>
              <div class="feedback">{{ answer.feedback }}</div>
            </div>
          </div>
        </div>

        <div class="submit-actions card">
          <el-button @click="$router.back()">返回</el-button>
          <el-button type="primary" @click="goToExam" v-if="canRetake">
            重新作答
          </el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="暂无作答记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMyPaperSubmission, getAvailableExamPapers } from '@/api/studentExam'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submission = ref(null)
const paper = ref(null)

const canRetake = computed(() => {
  if (!paper.value || !submission.value) return false
  if (submission.value.status === 'DRAFT') return true
  if (paper.value.maxAttempts === 0) return true
  return (submission.value.attemptNumber || 0) < (paper.value.maxAttempts || 1)
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const paperId = route.params.id
    const res = await getMyPaperSubmission(paperId)
    if (res.code === 200) {
      submission.value = res.data
    }
    const papers = await getAvailableExamPapers()
    if (papers.code === 200) {
      paper.value = (papers.data || []).find(p => p.id == paperId)
    }
  } finally {
    loading.value = false
  }
}

function showAnswer(question) {
  if (!question) return false
  return !!(question.correctAnswer || question.answerExplanation || question.referenceAnswer)
}

function getScoreTagType(answer) {
  if (!answer.question) return ''
  if ((answer.score || 0) >= answer.question.score) return 'success'
  if ((answer.score || 0) > 0) return 'warning'
  return 'danger'
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
  if (showAnswer(answer.question) && isCorrectOption(answer, opt, idx)) classes.push('correct')
  if (isWrongSelected(answer, opt, idx)) classes.push('wrong')
  return classes
}

function goToExam() {
  router.push(`/student/exam/${route.params.id}`)
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
.exam-result-container {
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

.result-header {
  .header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    h1 { margin: 0; font-size: 22px; color: #303133; }
  }
  .score { font-weight: 600; }
  .score.auto { color: #409eff; }
  .score.manual { color: #e6a23c; }
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
}

.submit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
