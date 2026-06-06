<template>
  <div class="exam-list-container">
    <div class="page-header">
      <h1 class="page-title">试卷列表</h1>
    </div>

    <div v-loading="loading" class="content-card">
      <el-table
        :data="papers"
        stripe
        style="width: 100%"
        empty-text="暂无可用试卷"
      >
        <el-table-column prop="title" label="试卷标题" min-width="200" />
        <el-table-column prop="description" label="说明" min-width="200">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="80" align="center" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="duration" label="时长" width="100" align="center">
          <template #default="{ row }">{{ row.duration }} 分钟</template>
        </el-table-column>
        <el-table-column label="考试时间" width="300" align="center">
          <template #default="{ row }">
            <span v-if="row.startTime || row.endTime">
              {{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}
            </span>
            <span v-else>不限时</span>
          </template>
        </el-table-column>
        <el-table-column label="作答状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag v-if="getSubmissionStatus(row)" :type="getSubmissionTag(row)">
              {{ getSubmissionStatus(row) }}
            </el-tag>
            <el-tag v-else type="info">未作答</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canTakeExam(row)"
              type="primary"
              size="small"
              @click="startExam(row)"
            >
              {{ getSubmission(row)?.status === 'DRAFT' ? '继续作答' : '开始考试' }}
            </el-button>
            <el-button
              v-else-if="getSubmission(row)"
              type="success"
              size="small"
              @click="viewResult(row)"
            >
              查看结果
            </el-button>
            <el-button
              v-else
              type="info"
              size="small"
              disabled
            >
              不可作答
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAvailableExamPapers, getMyExamSubmissions, startExam as startExamApi } from '@/api/studentExam'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const papers = ref([])
const mySubmissions = ref([])

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [paperRes, subRes] = await Promise.all([
      getAvailableExamPapers(),
      getMyExamSubmissions()
    ])
    if (paperRes.code === 200) {
      papers.value = paperRes.data || []
    }
    if (subRes.code === 200) {
      mySubmissions.value = subRes.data || []
    }
  } finally {
    loading.value = false
  }
}

function getSubmission(paper) {
  return mySubmissions.value.find(s => s.paperId === paper.id)
}

function getSubmissionStatus(paper) {
  const s = getSubmission(paper)
  if (!s) return null
  const map = { DRAFT: '草稿中', SUBMITTED: '已提交', PENDING_GRADING: '待批改', GRADED: '已批改' }
  return map[s.status] || '未知'
}

function getSubmissionTag(paper) {
  const s = getSubmission(paper)
  if (!s) return ''
  const map = { DRAFT: 'info', SUBMITTED: 'primary', PENDING_GRADING: 'warning', GRADED: 'success' }
  return map[s.status] || ''
}

function canTakeExam(paper) {
  const s = getSubmission(paper)
  if (!s) return true
  if (s.status === 'DRAFT') return true
  if (s.status === 'SUBMITTED' || s.status === 'PENDING_GRADING' || s.status === 'GRADED') {
    return paper.maxAttempts === 0 || (s.attemptNumber || 0) < (paper.maxAttempts || 1)
  }
  return false
}

async function startExam(paper) {
  try {
    const res = await startExamApi(paper.id)
    if (res.code === 200) {
      router.push(`/student/exam/${paper.id}`)
    } else {
      ElMessage.error(res.message || '进入考试失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '进入考试失败')
  }
}

function viewResult(paper) {
  router.push(`/student/exam-result/${paper.id}`)
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.exam-list-container {
  max-width: 1300px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>
