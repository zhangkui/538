<template>
  <div class="question-detail-container">
    <div v-loading="loading" class="content-wrapper">
      <template v-if="question">
        <div class="question-info card">
          <div class="question-header">
            <h1>{{ question.title }}</h1>
            <div class="question-meta">
              <el-tag :type="getQuestionTypeTag(question.questionType)" size="small">
                {{ getQuestionTypeText(question.questionType) }}
              </el-tag>
              <span :class="['difficulty-tag', question.difficulty?.toLowerCase()]">
                {{ getDifficultyText(question.difficulty) }}
              </span>
              <el-tag>{{ question.score }}分</el-tag>
            </div>
          </div>
          
          <el-divider />
          
          <div class="section">
            <h3>题目描述</h3>
            <p class="description">{{ question.description }}</p>
          </div>
          
          <template v-if="questionType === 'PROGRAMMING'">
            <div v-if="question.requirements" class="section">
              <h3>要求说明</h3>
              <pre class="requirements">{{ question.requirements }}</pre>
            </div>
            
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12">
                <div v-if="question.sampleInput" class="section">
                  <h3>示例输入</h3>
                  <pre class="code-block">{{ question.sampleInput }}</pre>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12">
                <div v-if="question.sampleOutput" class="section">
                  <h3>示例输出</h3>
                  <pre class="code-block">{{ question.sampleOutput }}</pre>
                </div>
              </el-col>
            </el-row>
          </template>
          
          <template v-else>
            <div v-if="parsedOptions && parsedOptions.length > 0" class="section">
              <h3>
                选项
                <span class="choice-hint">
                  {{ questionType === 'SINGLE_CHOICE' ? '（单选题，只能选一个）' : '（多选题，可选多个）' }}
                </span>
              </h3>
              <div class="options-container">
                <el-radio-group
                  v-if="questionType === 'SINGLE_CHOICE' && !submitted"
                  v-model="singleAnswer"
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
                  v-else-if="questionType === 'MULTIPLE_CHOICE' && !submitted"
                  v-model="multiAnswer"
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
                
                <div v-else class="choice-group">
                  <div
                    v-for="(opt, idx) in parsedOptions"
                    :key="idx"
                    :class="['choice-item', 'choice-result', getOptionClass(opt, idx)]"
                  >
                    <span class="option-key">{{ getOptionKey(opt, idx) }}.</span>
                    <span class="option-text">{{ opt }}</span>
                    <el-icon v-if="isCorrectOption(opt, idx)" class="icon-correct"><CircleCheck /></el-icon>
                    <el-icon v-else-if="isWrongSelected(opt, idx)" class="icon-wrong"><CircleClose /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
        
        <template v-if="!submitted">
          <div v-if="questionType === 'PROGRAMMING'" class="answer-section card">
            <h2>
              <el-icon><Edit /></el-icon>
              编写代码
            </h2>
            
            <el-form ref="formRef" :model="form" :rules="rules">
              <el-form-item prop="answerCode">
                <div class="code-editor">
                  <div class="editor-header">
                    <span>Java代码</span>
                    <el-button size="small" @click="clearCode">
                      <el-icon><Delete /></el-icon>
                      清空
                    </el-button>
                  </div>
                  <el-input
                    v-model="form.answerCode"
                    type="textarea"
                    :rows="15"
                    placeholder="请在此输入您的Java代码..."
                    class="code-textarea"
                  />
                </div>
              </el-form-item>
            </el-form>
            
            <div class="submit-actions">
              <el-button @click="$router.back()">
                <el-icon><Back /></el-icon>
                返回列表
              </el-button>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                <el-icon><Position /></el-icon>
                提交答案
              </el-button>
            </div>
          </div>
          
          <div v-else class="answer-section card">
            <h2>
              <el-icon><Edit /></el-icon>
              选择答案
            </h2>
            
            <div class="choice-submit-hint">
              <template v-if="questionType === 'SINGLE_CHOICE'">
                当前选择：<el-tag v-if="singleAnswer" type="primary">{{ singleAnswer }}</el-tag>
                <span v-else class="no-selection">未选择</span>
              </template>
              <template v-else>
                当前选择：
                <el-tag v-for="a in multiAnswer" :key="a" type="success" style="margin-right: 4px">{{ a }}</el-tag>
                <span v-if="multiAnswer.length === 0" class="no-selection">未选择</span>
              </template>
            </div>
            
            <div class="submit-actions">
              <el-button @click="$router.back()">
                <el-icon><Back /></el-icon>
                返回列表
              </el-button>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                <el-icon><Position /></el-icon>
                提交答案
              </el-button>
            </div>
          </div>
        </template>
        
        <div v-if="submitted && mySubmission" class="history-section card">
          <h2>
            <el-icon><Clock /></el-icon>
            提交结果
          </h2>
          
          <div class="submission-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="提交状态">
                <el-tag :type="mySubmission.status === 'GRADED' ? 'success' : 'warning'">
                  {{ mySubmission.status === 'GRADED' ? '已批改' : '待批改' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="得分" v-if="mySubmission.status === 'GRADED'">
                <span class="score">
                  {{ mySubmission.score }} / {{ question.score }}
                </span>
                <el-tag
                  v-if="questionType !== 'PROGRAMMING'"
                  :type="mySubmission.score === question.score ? 'success' : 'danger'"
                  style="margin-left: 8px"
                  size="small"
                >
                  {{ mySubmission.score === question.score ? '回答正确' : '回答错误' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="提交时间">
                {{ formatDate(mySubmission.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="批改老师" v-if="mySubmission.graderName">
                {{ mySubmission.graderName }}
              </el-descriptions-item>
              <el-descriptions-item label="你的答案" v-if="questionType !== 'PROGRAMMING'">
                <el-tag type="primary">{{ mySubmission.selectedAnswer || '-' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="标准答案" v-if="questionType !== 'PROGRAMMING'">
                <el-tag type="success">{{ mySubmission.correctAnswer || '-' }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <div v-if="questionType !== 'PROGRAMMING' && mySubmission.answerExplanation" class="feedback-section">
              <h4>
                <el-icon><Reading /></el-icon>
                答案解析
              </h4>
              <div class="explanation-content">{{ mySubmission.answerExplanation }}</div>
            </div>
            
            <div v-if="questionType === 'PROGRAMMING' && mySubmission.answerCode" class="feedback-section">
              <h4>
                <el-icon><Document /></el-icon>
                提交的代码
              </h4>
              <pre class="code-block">{{ mySubmission.answerCode }}</pre>
            </div>
            
            <div v-if="mySubmission.feedback" class="feedback-section">
              <h4>老师评语</h4>
              <div class="feedback-content">{{ mySubmission.feedback }}</div>
            </div>
          </div>
          
          <div class="submit-actions" style="margin-top: 20px">
            <el-button @click="$router.back()">
              <el-icon><Back /></el-icon>
              返回列表
            </el-button>
            <el-button type="primary" @click="reanswer">
              <el-icon><Refresh /></el-icon>
              重新作答
            </el-button>
          </div>
        </div>
      </template>
      
      <el-empty v-else-if="!loading" description="题目不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuestion, submitAnswer, getMySubmissions } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose, Reading, Refresh } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const question = ref(null)
const mySubmission = ref(null)
const formRef = ref(null)
const submitted = ref(false)
const singleAnswer = ref('')
const multiAnswer = ref([])

const form = reactive({
  answerCode: ''
})

const rules = {
  answerCode: [
    { required: true, message: '请输入答案代码', trigger: 'blur' }
  ]
}

const questionType = computed(() => question.value?.questionType || 'PROGRAMMING')

const parsedOptions = computed(() => {
  if (!question.value?.options) return []
  try {
    return JSON.parse(question.value.options)
  } catch (e) {
    return []
  }
})

function getOptionKey(opt, idx) {
  const m = opt.match(/^([A-Z])\./)
  return m ? m[1] : String.fromCharCode(65 + idx)
}

function isCorrectOption(opt, idx) {
  if (!mySubmission.value?.correctAnswer) return false
  const key = getOptionKey(opt, idx)
  return mySubmission.value.correctAnswer.split(',').map(s => s.trim()).includes(key)
}

function isWrongSelected(opt, idx) {
  if (!mySubmission.value?.selectedAnswer) return false
  const key = getOptionKey(opt, idx)
  const selected = mySubmission.value.selectedAnswer.split(',').map(s => s.trim())
  const correct = mySubmission.value.correctAnswer?.split(',').map(s => s.trim()) || []
  return selected.includes(key) && !correct.includes(key)
}

function getOptionClass(opt, idx) {
  const classes = []
  if (isCorrectOption(opt, idx)) classes.push('correct')
  if (isWrongSelected(opt, idx)) classes.push('wrong')
  return classes
}

onMounted(() => {
  loadQuestion()
})

async function loadQuestion() {
  loading.value = true
  try {
    const questionId = route.params.id
    const res = await getQuestion(questionId)
    if (res.code === 200) {
      question.value = res.data
      await loadMySubmission(questionId)
    } else {
      ElMessage.error(res.message || '加载题目失败')
    }
  } finally {
    loading.value = false
  }
}

async function loadMySubmission(questionId) {
  try {
    const res = await getMySubmissions()
    if (res.code === 200) {
      const submissions = res.data || []
      mySubmission.value = submissions.find(s => s.questionId == questionId)
      if (mySubmission.value) {
        submitted.value = true
        if (questionType.value === 'PROGRAMMING') {
          form.answerCode = mySubmission.value.answerCode || ''
        } else if (mySubmission.value.selectedAnswer) {
          if (questionType.value === 'SINGLE_CHOICE') {
            singleAnswer.value = mySubmission.value.selectedAnswer
          } else {
            multiAnswer.value = mySubmission.value.selectedAnswer.split(',').map(s => s.trim())
          }
        }
      }
    }
  } catch (error) {
    console.error('加载提交记录失败', error)
  }
}

async function handleSubmit() {
  if (questionType.value === 'PROGRAMMING') {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
  } else {
    if (questionType.value === 'SINGLE_CHOICE' && !singleAnswer.value) {
      ElMessage.warning('请选择答案')
      return
    }
    if (questionType.value === 'MULTIPLE_CHOICE' && multiAnswer.value.length === 0) {
      ElMessage.warning('请选择至少一个答案')
      return
    }
  }
  
  await ElMessageBox.confirm('确定要提交答案吗？提交后选择题将自动判分。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
  
  submitting.value = true
  try {
    const submitData = {
      questionId: question.value.id
    }
    
    if (questionType.value === 'PROGRAMMING') {
      submitData.answerCode = form.answerCode
    } else {
      submitData.selectedAnswer = questionType.value === 'SINGLE_CHOICE'
        ? singleAnswer.value
        : multiAnswer.value.join(',')
    }
    
    const res = await submitAnswer(submitData)
    
    if (res.code === 200) {
      ElMessage.success('提交成功')
      mySubmission.value = res.data
      submitted.value = true
    }
  } finally {
    submitting.value = false
  }
}

function reanswer() {
  submitted.value = false
  if (questionType.value === 'PROGRAMMING') {
    form.answerCode = ''
  } else {
    singleAnswer.value = ''
    multiAnswer.value = []
  }
  mySubmission.value = null
}

function clearCode() {
  ElMessageBox.confirm('确定要清空代码吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.answerCode = ''
  }).catch(() => {})
}

function getDifficultyText(difficulty) {
  const map = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return map[difficulty] || '未知'
}

function getQuestionTypeText(type) {
  const map = {
    'PROGRAMMING': '编程题',
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题'
  }
  return map[type] || '编程题'
}

function getQuestionTypeTag(type) {
  const map = {
    'PROGRAMMING': '',
    'SINGLE_CHOICE': 'warning',
    'MULTIPLE_CHOICE': 'success'
  }
  return map[type] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.question-detail-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.question-info {
  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 12px;
    
    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0;
    }
    
    .question-meta {
      display: flex;
      gap: 8px;
      align-items: center;
      flex-wrap: wrap;
    }
  }
  
  .section {
    margin-bottom: 20px;
    
    h3 {
      font-size: 16px;
      color: #303133;
      margin-bottom: 12px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .description {
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
    }
    
    .requirements {
      background: #f5f7fa;
      padding: 16px;
      border-radius: 8px;
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
      font-family: inherit;
      margin: 0;
    }
    
    .choice-hint {
      font-size: 13px;
      color: #909399;
      font-weight: normal;
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
  border: 1px solid transparent;
  transition: all 0.2s;
  
  .option-text {
    color: #303133;
    line-height: 1.6;
  }
  
  &.choice-result {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: default;
    
    .option-key {
      font-weight: 600;
      color: #303133;
    }
    
    .option-text {
      flex: 1;
    }
    
    .icon-correct {
      color: #67c23a;
      font-size: 20px;
    }
    
    .icon-wrong {
      color: #f56c6c;
      font-size: 20px;
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
  margin: 0;
}

.answer-section {
  h2 {
    font-size: 18px;
    color: #303133;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .code-editor {
    width: 100%;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    overflow: hidden;
    
    .editor-header {
      background: #f5f7fa;
      padding: 8px 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #dcdfe6;
      
      span {
        font-size: 13px;
        color: #606266;
      }
    }
    
    .code-textarea {
      :deep(.el-textarea__inner) {
        font-family: 'Consolas', 'Monaco', monospace;
        font-size: 14px;
        line-height: 1.6;
        border: none;
        border-radius: 0;
        resize: none;
      }
    }
  }
  
  .choice-submit-hint {
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    font-size: 14px;
    color: #606266;
    margin-bottom: 20px;
    
    .no-selection {
      color: #909399;
    }
  }
  
  .submit-actions {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

.history-section {
  h2 {
    font-size: 18px;
    color: #303133;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .score {
    font-size: 18px;
    font-weight: 600;
    color: #409eff;
  }
  
  .feedback-section {
    margin-top: 20px;
    
    h4 {
      font-size: 14px;
      color: #303133;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    
    .feedback-content {
      background: #f5f7fa;
      padding: 16px;
      border-radius: 8px;
      color: #606266;
      line-height: 1.6;
      border-left: 4px solid #409eff;
    }
    
    .explanation-content {
      background: #ecf5ff;
      padding: 16px;
      border-radius: 8px;
      color: #606266;
      line-height: 1.8;
      border-left: 4px solid #67c23a;
      white-space: pre-wrap;
    }
    
    .code-block {
      max-height: 300px;
      overflow-y: auto;
    }
  }
}

.difficulty-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  
  &.easy {
    background: rgba(103, 194, 58, 0.1);
    color: #67c23a;
  }
  
  &.medium {
    background: rgba(230, 162, 60, 0.1);
    color: #e6a23c;
  }
  
  &.hard {
    background: rgba(245, 108, 108, 0.1);
    color: #f56c6c;
  }
}
</style>
