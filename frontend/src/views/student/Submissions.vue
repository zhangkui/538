<template>
  <div class="submissions-container">
    <h1 class="page-title">我的提交</h1>
    
    <div v-loading="loading" class="submissions-content">
      <el-table
        :data="submissions"
        stripe
        style="width: 100%"
        empty-text="暂无提交记录"
      >
        <el-table-column prop="questionTitle" label="题目" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="goToQuestion(row.questionId)">
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
        
        <el-table-column prop="score" label="得分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'GRADED'" class="score">{{ row.score }}</span>
            <span v-else class="pending">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="graderName" label="批改老师" width="120" align="center">
          <template #default="{ row }">
            {{ row.graderName || (row.questionType && row.questionType !== 'PROGRAMMING' && row.status === 'GRADED' ? '系统自动' : '-') }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="提交时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <el-dialog
      v-model="dialogVisible"
      title="提交详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <template v-if="currentSubmission">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目">
            {{ currentSubmission.questionTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="题型">
            <el-tag :type="getQuestionTypeTag(currentSubmission.questionType)" size="small">
              {{ getQuestionTypeText(currentSubmission.questionType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentSubmission.status === 'GRADED' ? 'success' : 'warning'">
              {{ getStatusText(currentSubmission) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="得分" v-if="currentSubmission.status === 'GRADED'">
            <span class="score-large">{{ currentSubmission.score }}</span>
            <el-tag
              v-if="currentSubmission.questionType && currentSubmission.questionType !== 'PROGRAMMING'"
              :type="isAnswerCorrect ? 'success' : 'danger'"
              size="small"
              style="margin-left: 8px"
            >
              {{ isAnswerCorrect ? '回答正确' : '回答错误' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="批改老师" v-if="currentSubmission.graderName">
            {{ currentSubmission.graderName }}
          </el-descriptions-item>
          <el-descriptions-item label="批改老师" v-else-if="currentSubmission.questionType && currentSubmission.questionType !== 'PROGRAMMING' && currentSubmission.status === 'GRADED'">
            系统自动判分
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDate(currentSubmission.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="批改时间" v-if="currentSubmission.gradedAt">
            {{ formatDate(currentSubmission.gradedAt) }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentSubmission.questionType && currentSubmission.questionType !== 'PROGRAMMING'"
            label="你的选择"
            :span="2"
          >
            <el-tag type="primary">{{ currentSubmission.selectedAnswer || '未作答' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentSubmission.questionType && currentSubmission.questionType !== 'PROGRAMMING' && currentSubmission.status === 'GRADED'"
            label="正确答案"
            :span="2"
          >
            <el-tag type="success">{{ currentSubmission.correctAnswer || '-' }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="isProgramming && currentSubmission.answerCode" class="detail-section">
          <h4>提交代码</h4>
          <pre class="code-block">{{ currentSubmission.answerCode }}</pre>
        </div>
        
        <div
          v-if="!isProgramming && currentSubmission.answerExplanation"
          class="detail-section"
        >
          <h4>
            <el-icon><Reading /></el-icon>
            答案解析
          </h4>
          <div class="explanation-box">{{ currentSubmission.answerExplanation }}</div>
        </div>
        
        <div v-if="currentSubmission.feedback" class="detail-section">
          <h4>老师评语</h4>
          <div class="feedback-box">{{ currentSubmission.feedback }}</div>
        </div>
      </template>
      
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToQuestion(currentSubmission?.questionId)">
          重新作答
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMySubmissions, getQuestion } from '@/api/student'
import { Reading } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const submissions = ref([])
const dialogVisible = ref(false)
const currentSubmission = ref(null)
const questionDetail = ref(null)

const isProgramming = computed(() => {
  return !currentSubmission.value?.questionType || currentSubmission.value.questionType === 'PROGRAMMING'
})

const isAnswerCorrect = computed(() => {
  if (!currentSubmission.value || !questionDetail.value) return false
  return currentSubmission.value.score === questionDetail.value.score
})

onMounted(() => {
  loadSubmissions()
})

async function loadSubmissions() {
  loading.value = true
  try {
    const res = await getMySubmissions()
    if (res.code === 200) {
      submissions.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

async function showDetail(submission) {
  currentSubmission.value = submission
  questionDetail.value = null
  
  try {
    const qRes = await getQuestion(submission.questionId)
    if (qRes.code === 200) {
      questionDetail.value = qRes.data
    }
  } catch (e) {}
  
  dialogVisible.value = true
}

function goToQuestion(questionId) {
  dialogVisible.value = false
  router.push(`/student/question/${questionId}`)
}

function getQuestionTypeText(type) {
  const map = { 'PROGRAMMING': '编程题', 'SINGLE_CHOICE': '单选题', 'MULTIPLE_CHOICE': '多选题' }
  return map[type] || '编程题'
}

function getQuestionTypeTag(type) {
  const map = { 'PROGRAMMING': '', 'SINGLE_CHOICE': 'warning', 'MULTIPLE_CHOICE': 'success' }
  return map[type] || ''
}

function getStatusText(row) {
  if (row.questionType && row.questionType !== 'PROGRAMMING' && row.status === 'GRADED') {
    return '已自动判分'
  }
  return row.status === 'GRADED' ? '已批改' : '待批改'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.submissions-container {
  max-width: 1200px;
  margin: 0 auto;
}

.submissions-content {
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

.pending {
  color: #909399;
}

.score-large {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}

.detail-section {
  margin-top: 20px;
  
  h4 {
    font-size: 14px;
    color: #303133;
    margin-bottom: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 6px;
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
  max-height: 300px;
  overflow-y: auto;
}

.feedback-box {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  color: #606266;
  line-height: 1.6;
  border-left: 4px solid #409eff;
}

.explanation-box {
  background: #ecf5ff;
  padding: 16px;
  border-radius: 8px;
  color: #606266;
  line-height: 1.8;
  border-left: 4px solid #67c23a;
  white-space: pre-wrap;
}
</style>
