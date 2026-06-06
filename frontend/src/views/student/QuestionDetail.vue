<template>
  <div class="question-detail-container">
    <div v-loading="loading" class="content-wrapper">
      <template v-if="question">
        <!-- 题目信息 -->
        <div class="question-info card">
          <div class="question-header">
            <h1>{{ question.title }}</h1>
            <div class="question-meta">
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
        </div>
        
        <!-- 答题区域 -->
        <div class="answer-section card">
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
        
        <!-- 历史提交 -->
        <div v-if="mySubmission" class="history-section card">
          <h2>
            <el-icon><Clock /></el-icon>
            我的提交
          </h2>
          
          <div class="submission-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="提交状态">
                <el-tag :type="mySubmission.status === 'GRADED' ? 'success' : 'warning'">
                  {{ mySubmission.status === 'GRADED' ? '已批改' : '待批改' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="得分" v-if="mySubmission.status === 'GRADED'">
                <span class="score">{{ mySubmission.score }} / {{ question.score }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="提交时间">
                {{ formatDate(mySubmission.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="批改老师" v-if="mySubmission.graderName">
                {{ mySubmission.graderName }}
              </el-descriptions-item>
            </el-descriptions>
            
            <div v-if="mySubmission.feedback" class="feedback-section">
              <h4>老师评语</h4>
              <div class="feedback-content">{{ mySubmission.feedback }}</div>
            </div>
          </div>
        </div>
      </template>
      
      <el-empty v-else-if="!loading" description="题目不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuestion, submitAnswer, getMySubmissions } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const question = ref(null)
const mySubmission = ref(null)
const formRef = ref(null)

const form = reactive({
  answerCode: ''
})

const rules = {
  answerCode: [
    { required: true, message: '请输入答案代码', trigger: 'blur' }
  ]
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
        form.answerCode = mySubmission.value.answerCode
      }
    }
  } catch (error) {
    console.error('加载提交记录失败', error)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  await ElMessageBox.confirm('确定要提交答案吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
  
  submitting.value = true
  try {
    const res = await submitAnswer({
      questionId: question.value.id,
      answerCode: form.answerCode
    })
    
    if (res.code === 200) {
      ElMessage.success('提交成功')
      mySubmission.value = res.data
    }
  } finally {
    submitting.value = false
  }
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
    }
  }
  
  .section {
    margin-bottom: 20px;
    
    h3 {
      font-size: 16px;
      color: #303133;
      margin-bottom: 12px;
      font-weight: 600;
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
    }
    
    .feedback-content {
      background: #f5f7fa;
      padding: 16px;
      border-radius: 8px;
      color: #606266;
      line-height: 1.6;
      border-left: 4px solid #409eff;
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
