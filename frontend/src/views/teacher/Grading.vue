<template>
  <div class="grading-container">
    <h1 class="page-title">批改作业</h1>
    
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" @change="loadSubmissions">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">
          待批改
          <el-badge v-if="pendingCount > 0" :value="pendingCount" class="badge" />
        </el-radio-button>
        <el-radio-button label="GRADED">已批改</el-radio-button>
      </el-radio-group>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索学生/题目..."
        prefix-icon="Search"
        clearable
        style="width: 240px"
      />
    </div>
    
    <div v-loading="loading" class="grading-content">
      <el-table
        :data="filteredSubmissions"
        stripe
        style="width: 100%"
        empty-text="暂无提交记录"
      >
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="questionTitle" label="题目" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="showGradeDialog(row)">
              {{ row.questionTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getQuestionTypeTag(row.questionType)" size="small">
              {{ getQuestionTypeText(row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'GRADED' ? 'success' : 'warning'" size="small">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'GRADED'" class="score">{{ row.score }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showGradeDialog(row)">
              {{ row.status === 'GRADED' ? '查看/修改' : '批改' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <el-dialog
      v-model="dialogVisible"
      :title="`批改 - ${currentSubmission?.questionTitle || ''}`"
      width="900px"
      :close-on-click-modal="false"
      top="5vh"
    >
      <template v-if="currentSubmission">
        <div class="grade-dialog-content">
          <el-descriptions :column="2" border style="margin-bottom: 20px">
            <el-descriptions-item label="学生">
              {{ currentSubmission.studentName }}
            </el-descriptions-item>
            <el-descriptions-item label="题型">
              <el-tag :type="getQuestionTypeTag(currentSubmission.questionType)" size="small">
                {{ getQuestionTypeText(currentSubmission.questionType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentSubmission.status === 'GRADED' ? 'success' : 'warning'" size="small">
                {{ getStatusText(currentSubmission) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ formatDate(currentSubmission.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>
          
          <el-row :gutter="20">
            <el-col :span="14">
              <template v-if="isProgramming">
                <div class="section-card">
                  <h3>
                    <el-icon><User /></el-icon>
                    {{ currentSubmission.studentName }} 的答案代码
                  </h3>
                  <pre class="code-block">{{ currentSubmission.answerCode }}</pre>
                </div>
              </template>
              <template v-else>
                <div class="section-card">
                  <h3>
                    <el-icon><User /></el-icon>
                    {{ currentSubmission.studentName }} 的选择
                  </h3>
                  <div class="answer-display">
                    <div class="answer-row">
                      <span class="answer-label">学生选择：</span>
                      <el-tag type="primary" size="large">
                        {{ currentSubmission.selectedAnswer || '未作答' }}
                      </el-tag>
                    </div>
                    <div class="answer-row" v-if="questionDetail?.options">
                      <span class="answer-label">选项列表：</span>
                      <div class="options-list">
                        <div
                          v-for="(opt, idx) in parsedOptions"
                          :key="idx"
                          :class="['option-row', getTeacherOptionClass(opt, idx)]"
                        >
                          <span class="option-key">{{ getOptionKey(opt, idx) }}.</span>
                          <span class="option-text">{{ opt }}</span>
                          <el-icon v-if="isCorrectOptionForTeacher(opt, idx)" class="icon-correct"><CircleCheck /></el-icon>
                          <el-icon v-else-if="isWrongSelectedForTeacher(opt, idx)" class="icon-wrong"><CircleClose /></el-icon>
                        </div>
                      </div>
                    </div>
                    <div v-if="currentSubmission.answerExplanation" class="explanation-section">
                      <h4><el-icon><Reading /></el-icon>答案解析</h4>
                      <div class="explanation-content">{{ currentSubmission.answerExplanation }}</div>
                    </div>
                  </div>
                </div>
              </template>
            </el-col>
            
            <el-col :span="10">
              <template v-if="isProgramming && questionDetail?.referenceAnswer">
                <div class="section-card">
                  <h3>
                    <el-icon><Document /></el-icon>
                    参考答案
                  </h3>
                  <pre class="code-block reference">{{ questionDetail.referenceAnswer }}</pre>
                </div>
              </template>
              <template v-else-if="!isProgramming">
                <div class="section-card">
                  <h3>
                    <el-icon><Document /></el-icon>
                    标准答案
                  </h3>
                  <div class="standard-answer">
                    <el-tag type="success" size="large">
                      {{ currentSubmission.correctAnswer || questionDetail?.correctAnswer || '-' }}
                    </el-tag>
                    <div class="auto-grade-hint" v-if="currentSubmission.status === 'GRADED'">
                      <el-tag :type="isAnswerCorrect ? 'success' : 'danger'" size="small">
                        系统自动判分：{{ isAnswerCorrect ? '正确' : '错误' }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </template>
              
              <div class="section-card grading-form">
                <h3>
                  <el-icon><Edit /></el-icon>
                  评分
                </h3>
                <el-form :model="gradeForm" label-position="top">
                  <el-form-item label="得分">
                    <el-input-number
                      v-model="gradeForm.score"
                      :min="0"
                      :max="questionDetail?.score || 100"
                      style="width: 100%"
                      :disabled="!isProgramming && currentSubmission.status === 'GRADED'"
                    />
                    <div class="score-hint">
                      满分：{{ questionDetail?.score || '-' }} 分
                      <span v-if="!isProgramming" class="auto-hint">
                        （选择题由系统自动判分，可手动调整）
                      </span>
                    </div>
                  </el-form-item>
                  
                  <el-form-item label="评语">
                    <el-input
                      v-model="gradeForm.feedback"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入评语（可选）"
                    />
                  </el-form-item>
                </el-form>
              </div>
            </el-col>
          </el-row>
        </div>
      </template>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleGrade">
          {{ currentSubmission?.status === 'GRADED' ? '保存修改' : '确认批改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAllSubmissions, getPendingSubmissions, getSubmission, gradeSubmission, getQuestions } from '@/api/teacher'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, Reading } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const submissions = ref([])
const questions = ref([])
const filterStatus = ref('PENDING')
const searchKeyword = ref('')
const dialogVisible = ref(false)
const currentSubmission = ref(null)
const questionDetail = ref(null)

const gradeForm = reactive({
  score: 0,
  feedback: ''
})

const pendingCount = computed(() => {
  return submissions.value.filter(s => s.status === 'PENDING').length
})

const filteredSubmissions = computed(() => {
  return submissions.value.filter(s => {
    const matchStatus = !filterStatus.value || s.status === filterStatus.value
    const matchKeyword = !searchKeyword.value ||
      s.studentName?.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      s.questionTitle?.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return matchStatus && matchKeyword
  })
})

const isProgramming = computed(() => {
  return !currentSubmission.value?.questionType || currentSubmission.value.questionType === 'PROGRAMMING'
})

const parsedOptions = computed(() => {
  if (!questionDetail.value?.options) return []
  try {
    return JSON.parse(questionDetail.value.options)
  } catch (e) {
    return []
  }
})

const isAnswerCorrect = computed(() => {
  if (!currentSubmission.value || !questionDetail.value) return false
  return currentSubmission.value.score === questionDetail.value.score
})

function getOptionKey(opt, idx) {
  const m = opt.match(/^([A-Z])\./)
  return m ? m[1] : String.fromCharCode(65 + idx)
}

function isCorrectOptionForTeacher(opt, idx) {
  const correct = currentSubmission.value?.correctAnswer || questionDetail.value?.correctAnswer
  if (!correct) return false
  const key = getOptionKey(opt, idx)
  return correct.split(',').map(s => s.trim()).includes(key)
}

function isWrongSelectedForTeacher(opt, idx) {
  if (!currentSubmission.value?.selectedAnswer) return false
  const key = getOptionKey(opt, idx)
  const selected = currentSubmission.value.selectedAnswer.split(',').map(s => s.trim())
  const correct = (currentSubmission.value.correctAnswer || questionDetail.value?.correctAnswer || '')
    .split(',').map(s => s.trim())
  return selected.includes(key) && !correct.includes(key)
}

function getTeacherOptionClass(opt, idx) {
  const classes = []
  if (isCorrectOptionForTeacher(opt, idx)) classes.push('correct')
  if (isWrongSelectedForTeacher(opt, idx)) classes.push('wrong')
  return classes
}

onMounted(() => {
  loadSubmissions()
  loadQuestions()
})

async function loadSubmissions() {
  loading.value = true
  try {
    const res = await getAllSubmissions()
    if (res.code === 200) {
      submissions.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

async function loadQuestions() {
  try {
    const res = await getQuestions()
    if (res.code === 200) {
      questions.value = res.data || []
    }
  } catch (error) {
    console.error('加载题目失败', error)
  }
}

function getStatusText(row) {
  if (row.questionType && row.questionType !== 'PROGRAMMING' && row.status === 'GRADED') {
    return '已自动判分'
  }
  return row.status === 'GRADED' ? '已批改' : '待批改'
}

async function showGradeDialog(submission) {
  currentSubmission.value = { ...submission }
  
  questionDetail.value = questions.value.find(q => q.id === submission.questionId)
  
  gradeForm.score = submission.status === 'GRADED' ? (submission.score || 0) : 0
  gradeForm.feedback = submission.feedback || ''
  
  dialogVisible.value = true
}

async function handleGrade() {
  if (gradeForm.score < 0) {
    ElMessage.warning('得分不能小于0')
    return
  }
  
  submitting.value = true
  try {
    const res = await gradeSubmission(currentSubmission.value.id, {
      score: gradeForm.score,
      feedback: gradeForm.feedback
    })
    
    if (res.code === 200) {
      ElMessage.success('批改成功')
      dialogVisible.value = false
      await loadSubmissions()
    }
  } finally {
    submitting.value = false
  }
}

function getQuestionTypeText(type) {
  const map = { 'PROGRAMMING': '编程题', 'SINGLE_CHOICE': '单选题', 'MULTIPLE_CHOICE': '多选题' }
  return map[type] || '编程题'
}

function getQuestionTypeTag(type) {
  const map = { 'PROGRAMMING': '', 'SINGLE_CHOICE': 'warning', 'MULTIPLE_CHOICE': 'success' }
  return map[type] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.grading-container {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
  
  .badge {
    margin-left: 8px;
  }
}

.grading-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.score {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.grade-dialog-content {
  max-height: 70vh;
  overflow-y: auto;
}

.section-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  
  h3 {
    font-size: 14px;
    color: #303133;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  h4 {
    font-size: 14px;
    color: #303133;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  &.grading-form {
    background: #fff;
    border: 1px solid #e4e7ed;
  }
}

.answer-display {
  .answer-row {
    margin-bottom: 16px;
    display: flex;
    align-items: flex-start;
    gap: 8px;
    flex-wrap: wrap;
    
    .answer-label {
      font-weight: 600;
      color: #606266;
      flex-shrink: 0;
      line-height: 24px;
    }
  }
  
  .options-list {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .option-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: #fff;
    border-radius: 6px;
    border: 1px solid transparent;
    
    .option-key {
      font-weight: 600;
      color: #303133;
    }
    
    .option-text {
      flex: 1;
      color: #606266;
      line-height: 1.6;
    }
    
    .icon-correct {
      color: #67c23a;
      font-size: 18px;
    }
    
    .icon-wrong {
      color: #f56c6c;
      font-size: 18px;
    }
    
    &.correct {
      background: #f0f9eb;
      border-color: #c2e7b0;
    }
    
    &.wrong {
      background: #fef0f0;
      border-color: #fbc4c4;
    }
  }
  
  .explanation-section {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
    
    .explanation-content {
      background: #ecf5ff;
      padding: 12px;
      border-radius: 6px;
      color: #606266;
      line-height: 1.8;
      border-left: 3px solid #67c23a;
      white-space: pre-wrap;
    }
  }
}

.standard-answer {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  
  .auto-grade-hint {
    margin-left: auto;
  }
}

.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
  
  &.reference {
    max-height: 200px;
    background: #2d3748;
  }
}

.score-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  
  .auto-hint {
    margin-left: 8px;
    color: #e6a23c;
  }
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .el-col {
    width: 100% !important;
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
}
</style>
