<template>
  <div class="exam-taking-container">
    <div v-loading="loading" class="content-wrapper">
      <template v-if="paper && submission">
        <div class="exam-header">
          <div class="paper-info">
            <h1>{{ paper.title }}</h1>
            <div class="meta-row">
              <el-tag size="small">共 {{ paper.questions?.length || 0 }} 题</el-tag>
              <el-tag size="small" type="success">总分 {{ paper.totalScore }} 分</el-tag>
              <el-tag size="small" type="warning">时长 {{ paper.duration }} 分钟</el-tag>
            </div>
          </div>
          <div class="timer-info">
            <div v-if="remainingTime > 0" class="timer">
              <el-icon :size="20"><Timer /></el-icon>
              <span class="time-text">{{ formatTime(remainingTime) }}</span>
            </div>
            <div v-else class="timer expired">
              <span>时间已到</span>
            </div>
          </div>
        </div>

        <div class="exam-body">
          <div class="question-nav">
            <div class="nav-title">题目导航</div>
            <div class="nav-grid">
              <div
                v-for="(q, idx) in paper.questions"
                :key="q.id"
                :class="['nav-item', { active: currentIdx === idx, answered: isAnswered(q) }]"
                @click="goToQuestion(idx)"
              >
                {{ idx + 1 }}
              </div>
            </div>
            <div class="nav-legend">
              <span><i class="dot answered"></i>已作答</span>
              <span><i class="dot"></i>未作答</span>
            </div>
          </div>

          <div class="question-area">
            <div v-if="currentQuestion" class="question-card">
              <div class="q-header">
                <div class="q-title-row">
                  <span class="q-num">第 {{ currentIdx + 1 }} 题</span>
                  <el-tag :type="getQuestionTypeTag(currentQuestion.questionType)" size="small">
                    {{ getQuestionTypeText(currentQuestion.questionType) }}
                  </el-tag>
                  <span class="q-score">（{{ currentQuestion.score }} 分）</span>
                  <span class="q-title-text">{{ currentQuestion.title }}</span>
                </div>
              </div>

              <div class="q-body">
                <div class="section">
                  <div class="section-title">题目描述</div>
                  <div class="description">{{ currentQuestion.description }}</div>
                </div>

                <template v-if="currentQuestion.questionType === 'PROGRAMMING'">
                  <div v-if="currentQuestion.requirements" class="section">
                    <div class="section-title">要求说明</div>
                    <pre class="requirements">{{ currentQuestion.requirements }}</pre>
                  </div>
                  <el-row :gutter="20" v-if="currentQuestion.sampleInput || currentQuestion.sampleOutput">
                    <el-col :xs="24" :sm="12" v-if="currentQuestion.sampleInput">
                      <div class="section">
                        <div class="section-title">示例输入</div>
                        <pre class="code-block">{{ currentQuestion.sampleInput }}</pre>
                      </div>
                    </el-col>
                    <el-col :xs="24" :sm="12" v-if="currentQuestion.sampleOutput">
                      <div class="section">
                        <div class="section-title">示例输出</div>
                        <pre class="code-block">{{ currentQuestion.sampleOutput }}</pre>
                      </div>
                    </el-col>
                  </el-row>

                  <div class="section">
                    <div class="section-title">
                      编写代码
                      <el-button size="small" @click="clearCode(currentQuestion.id)">
                        <el-icon><Delete /></el-icon>
                        清空
                      </el-button>
                    </div>
                    <div class="code-editor">
                      <el-input
                        v-model="answerMap[currentQuestion.id].answerCode"
                        type="textarea"
                        :rows="15"
                        placeholder="请在此输入您的Java代码..."
                        class="code-textarea"
                      />
                    </div>
                  </div>
                </template>

                <template v-else>
                  <div v-if="parsedOptions.length > 0" class="section">
                    <div class="section-title">
                      选项
                      <span class="hint">
                        {{ currentQuestion.questionType === 'SINGLE_CHOICE' ? '（单选题）' : '（多选题）' }}
                      </span>
                    </div>
                    <div class="options-container">
                      <el-radio-group
                        v-if="currentQuestion.questionType === 'SINGLE_CHOICE'"
                        v-model="answerMap[currentQuestion.id].singleAnswer"
                        class="choice-group"
                      >
                        <div
                          v-for="(opt, idx) in parsedOptions"
                          :key="idx"
                          class="choice-item"
                        >
                          <el-radio :label="getOptionKey(opt, idx)">
                            <span class="option-text">{{ opt }}</span>
                          </el-radio>
                        </div>
                      </el-radio-group>

                      <el-checkbox-group
                        v-else
                        v-model="answerMap[currentQuestion.id].multiAnswer"
                        class="choice-group"
                      >
                        <div
                          v-for="(opt, idx) in parsedOptions"
                          :key="idx"
                          class="choice-item"
                        >
                          <el-checkbox :label="getOptionKey(opt, idx)">
                            <span class="option-text">{{ opt }}</span>
                          </el-checkbox>
                        </div>
                      </el-checkbox-group>
                    </div>
                  </div>
                </template>
              </div>

              <div class="q-footer">
                <el-button :disabled="currentIdx === 0" @click="prevQuestion">
                  <el-icon><ArrowLeft /></el-icon>
                  上一题
                </el-button>
                <el-button
                  type="primary"
                  :disabled="currentIdx === (paper.questions?.length || 1) - 1"
                  @click="nextQuestion"
                >
                  下一题
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="exam-footer">
          <el-button @click="$router.back()">返回列表</el-button>
          <el-button @click="handleSaveDraft" :loading="saving">
            <el-icon><Document /></el-icon>
            保存草稿
          </el-button>
          <el-button type="danger" @click="handleSubmit" :loading="submitting">
            <el-icon><Position /></el-icon>
            提交试卷
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getExamPaperDetail,
  getMyPaperSubmission,
  startExam,
  saveExamDraft,
  submitExamPaper
} from '@/api/studentExam'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Delete, ArrowLeft, ArrowRight, Document, Position } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const paper = ref(null)
const submission = ref(null)
const currentIdx = ref(0)
const answerMap = reactive({})
const remainingTime = ref(0)
let timer = null

const currentQuestion = computed(() => paper.value?.questions?.[currentIdx.value])

const parsedOptions = computed(() => {
  if (!currentQuestion.value?.options) return []
  try { return JSON.parse(currentQuestion.value.options) } catch (e) { return [] }
})

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function loadData() {
  loading.value = true
  try {
    const paperId = route.params.id
    const [paperRes, subRes] = await Promise.all([
      getExamPaperDetail(paperId),
      getMyPaperSubmission(paperId)
    ])
    if (paperRes.code === 200) {
      paper.value = paperRes.data
      initAnswerMap(paperRes.data)
    }
    if (subRes.code === 200 && subRes.data) {
      submission.value = subRes.data
      restoreAnswers(subRes.data)
    } else {
      const startRes = await startExam(paperId)
      if (startRes.code === 200) {
        submission.value = startRes.data
      }
    }
    if (paper.value?.duration) {
      startTimer(paper.value.duration)
    }
  } finally {
    loading.value = false
  }
}

function initAnswerMap(p) {
  for (const q of p.questions || []) {
    if (!answerMap[q.id]) {
      answerMap[q.id] = {
        answerCode: '',
        singleAnswer: '',
        multiAnswer: []
      }
    }
  }
}

function restoreAnswers(s) {
  for (const a of s.answers || []) {
    if (a.question) {
      answerMap[a.paperQuestionId] = {
        answerCode: a.answerCode || '',
        singleAnswer: a.selectedAnswer || '',
        multiAnswer: a.selectedAnswer ? a.selectedAnswer.split(',').map(x => x.trim()) : []
      }
    }
  }
}

function startTimer(minutes) {
  remainingTime.value = minutes * 60
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('考试时间已到，请立即提交')
    }
  }, 1000)
}

function formatTime(seconds) {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) {
    return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function getOptionKey(opt, idx) {
  const m = opt.match(/^([A-Z])\./)
  return m ? m[1] : String.fromCharCode(65 + idx)
}

function isAnswered(q) {
  const a = answerMap[q.id]
  if (!a) return false
  if (q.questionType === 'PROGRAMMING') return !!(a.answerCode && a.answerCode.trim())
  if (q.questionType === 'SINGLE_CHOICE') return !!a.singleAnswer
  if (q.questionType === 'MULTIPLE_CHOICE') return (a.multiAnswer?.length || 0) > 0
  return false
}

function goToQuestion(idx) {
  currentIdx.value = idx
}

function prevQuestion() {
  if (currentIdx.value > 0) currentIdx.value--
}

function nextQuestion() {
  if (currentIdx.value < (paper.value.questions?.length || 1) - 1) currentIdx.value++
}

function clearCode(qid) {
  ElMessageBox.confirm('确定要清空代码吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    answerMap[qid].answerCode = ''
  }).catch(() => {})
}

function buildSubmitData() {
  const answers = []
  for (const q of paper.value.questions || []) {
    const a = answerMap[q.id]
    answers.push({
      paperQuestionId: q.id,
      answerCode: a?.answerCode || '',
      selectedAnswer: q.questionType === 'SINGLE_CHOICE'
        ? (a?.singleAnswer || '')
        : q.questionType === 'MULTIPLE_CHOICE'
          ? (a?.multiAnswer || []).join(',')
          : ''
    })
  }
  return { paperId: paper.value.id, answers }
}

async function handleSaveDraft() {
  saving.value = true
  try {
    const data = buildSubmitData()
    const res = await saveExamDraft(data)
    if (res.code === 200) {
      ElMessage.success('草稿已保存')
      submission.value = res.data
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {}
  finally {
    saving.value = false
  }
}

async function handleSubmit() {
  const unanswered = (paper.value.questions || []).filter(q => !isAnswered(q))
  if (unanswered.length > 0) {
    await ElMessageBox.confirm(
      `还有 ${unanswered.length} 道题未作答，确定要提交吗？`,
      '提示',
      { confirmButtonText: '确定提交', cancelButtonText: '继续作答', type: 'warning' }
    )
  } else {
    await ElMessageBox.confirm('确定要提交试卷吗？提交后选择题将自动判分。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
  }

  submitting.value = true
  try {
    const data = buildSubmitData()
    const res = await submitExamPaper(data)
    if (res.code === 200) {
      ElMessage.success('提交成功')
      router.push(`/student/exam-result/${paper.value.id}`)
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {}
  finally {
    submitting.value = false
  }
}

function getQuestionTypeText(type) {
  const map = { PROGRAMMING: '编程题', SINGLE_CHOICE: '单选题', MULTIPLE_CHOICE: '多选题' }
  return map[type] || '编程题'
}

function getQuestionTypeTag(type) {
  const map = { PROGRAMMING: '', SINGLE_CHOICE: 'warning', MULTIPLE_CHOICE: 'success' }
  return map[type] || ''
}
</script>

<style lang="scss" scoped>
.exam-taking-container {
  max-width: 1400px;
  margin: 0 auto;
  padding-bottom: 20px;
}

.exam-header {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .paper-info h1 {
    margin: 0 0 10px;
    font-size: 22px;
    color: #303133;
  }
  .meta-row {
    display: flex;
    gap: 8px;
  }
  .timer {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #409eff;
    font-size: 20px;
    font-weight: 600;
    padding: 8px 16px;
    background: #ecf5ff;
    border-radius: 8px;
    &.expired {
      color: #f56c6c;
      background: #fef0f0;
      font-size: 14px;
    }
  }
}

.exam-body {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.question-nav {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: fit-content;
  position: sticky;
  top: 16px;

  .nav-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }
  .nav-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
    margin-bottom: 16px;
  }
  .nav-item {
    aspect-ratio: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    color: #606266;
    transition: all 0.2s;
    &:hover { border-color: #409eff; color: #409eff; }
    &.active { background: #409eff; color: #fff; border-color: #409eff; }
    &.answered { border-color: #67c23a; color: #67c23a; }
    &.active.answered { background: #409eff; color: #fff; border-color: #409eff; }
  }
  .nav-legend {
    display: flex;
    flex-direction: column;
    gap: 8px;
    font-size: 12px;
    color: #909399;
    .dot {
      display: inline-block;
      width: 10px;
      height: 10px;
      border-radius: 2px;
      border: 1px solid #dcdfe6;
      margin-right: 6px;
      vertical-align: middle;
      &.answered { border-color: #67c23a; background: rgba(103, 194, 58, 0.1); }
    }
  }
}

.question-area {
  flex: 1;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .q-header {
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 20px;
    .q-title-row {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      .q-num {
        font-size: 18px;
        font-weight: 700;
        color: #409eff;
      }
      .q-score { color: #909399; font-size: 13px; }
      .q-title-text { font-size: 16px; font-weight: 600; color: #303133; }
    }
  }

  .q-body {
    .section { margin-bottom: 20px; }
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      gap: 8px;
      .hint { font-weight: normal; font-size: 12px; color: #909399; }
    }
    .description {
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
    }
    .requirements {
      background: #f5f7fa;
      padding: 14px;
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
    }
    .code-editor {
      border: 1px solid #dcdfe6;
      border-radius: 8px;
      overflow: hidden;
      :deep(.el-textarea__inner) {
        font-family: Consolas, Monaco, monospace;
        font-size: 14px;
        line-height: 1.6;
        border: none;
        resize: vertical;
        min-height: 300px;
      }
    }
  }

  .options-container {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
  }
  .choice-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .choice-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background: #fff;
    border-radius: 6px;
    .option-text {
      color: #303133;
      line-height: 1.6;
    }
  }

  .q-footer {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
  }
}

.exam-footer {
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  bottom: 0;
  z-index: 10;
}
</style>
