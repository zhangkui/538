<template>
  <div class="exam-record-container">
    <div class="page-header">
      <h1 class="page-title">我的试卷记录</h1>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 160px">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已提交" value="SUBMITTED" />
        <el-option label="待批改" value="PENDING_GRADING" />
        <el-option label="已批改" value="GRADED" />
      </el-select>
    </div>

    <div v-loading="loading" class="content-card">
      <el-table
        :data="filteredSubmissions"
        stripe
        style="width: 100%"
        empty-text="暂无答卷记录"
      >
        <el-table-column prop="paperTitle" label="试卷名称" min-width="200" />
        <el-table-column prop="attemptNumber" label="第几次" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="得分" width="140" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'GRADED'" class="score-text">
              {{ row.totalScore }} / {{ row.paperTotalScore }}
            </span>
            <span v-else-if="row.status === 'PENDING_GRADING'">
              自动: {{ row.autoScore || 0 }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" align="center">
          <template #default="{ row }">{{ formatDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column prop="gradedAt" label="批改时间" width="180" align="center">
          <template #default="{ row }">{{ formatDate(row.gradedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="viewDetail(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyExamSubmissions } from '@/api/studentExam'

const router = useRouter()
const loading = ref(false)
const submissions = ref([])
const filterStatus = ref('')

const filteredSubmissions = computed(() => {
  if (!filterStatus.value) return submissions.value
  return submissions.value.filter(s => s.status === filterStatus.value)
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMyExamSubmissions()
    if (res.code === 200) {
      submissions.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

function viewDetail(row) {
  router.push(`/student/exam-result/${row.paperId}`)
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
.exam-record-container {
  max-width: 1300px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.score-text {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}
</style>
