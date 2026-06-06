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
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'GRADED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'GRADED' ? '已批改' : '待批改' }}
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
    
    <!-- 批改弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`批改 - ${currentSubmission?.questionTitle || ''}`"
      width="900px"
      :close-on-click-modal="false"
      top="5vh"
    >
      <template v-if="currentSubmission">
        <div class="grade-dialog-content">
          <el-row :gutter="20">
            <!-- 左侧：学生代码 -->
            <el-col :span="14">
              <div class="section-card">
                <h3>
                  <el-icon><User /></el-icon>
                  {{ currentSubmission.studentName }} 的答案
                </h3>
                <pre class="code-block">{{ currentSubmission.answerCode }}</pre>
              </div>
            </el-col>
            
            <!-- 右侧：参考答案和评分 -->
            <el-col :span="10">
              <div v-if="questionDetail?.referenceAnswer" class="section-card">
                <h3>
                  <el-icon><Document /></el-icon>
                  参考答案
                </h3>
                <pre class="code-block reference">{{ questionDetail.referenceAnswer }}</pre>
              </div>
              
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
                    />
                    <div class="score-hint">满分：{{ questionDetail?.score || '-' }} 分</div>
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
          确认批改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAllSubmissions, getPendingSubmissions, getSubmission, gradeSubmission, getQuestions } from '@/api/teacher'
import { ElMessage } from 'element-plus'

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

async function showGradeDialog(submission) {
  currentSubmission.value = submission
  
  // 获取题目详情（包含参考答案）
  questionDetail.value = questions.value.find(q => q.id === submission.questionId)
  
  // 设置表单初始值
  gradeForm.score = submission.status === 'GRADED' ? submission.score : 0
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
  
  &.grading-form {
    background: #fff;
    border: 1px solid #e4e7ed;
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
