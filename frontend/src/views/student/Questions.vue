<template>
  <div class="questions-container">
    <div class="page-header">
      <h1 class="page-title">题目列表</h1>
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索题目..."
          prefix-icon="Search"
          clearable
          style="width: 240px"
        />
        <el-select v-model="filterDifficulty" placeholder="难度筛选" clearable style="width: 120px">
          <el-option label="简单" value="EASY" />
          <el-option label="中等" value="MEDIUM" />
          <el-option label="困难" value="HARD" />
        </el-select>
      </div>
    </div>
    
    <div v-loading="loading" class="questions-list">
      <template v-if="filteredQuestions.length > 0">
        <div
          v-for="question in filteredQuestions"
          :key="question.id"
          class="question-card"
          @click="goToQuestion(question.id)"
        >
          <div class="question-header">
            <h3 class="question-title">{{ question.title }}</h3>
            <div class="question-tags">
              <span :class="['difficulty-tag', question.difficulty?.toLowerCase()]">
                {{ getDifficultyText(question.difficulty) }}
              </span>
              <el-tag size="small">{{ question.score }}分</el-tag>
            </div>
          </div>
          <p class="question-desc">{{ truncateText(question.description, 120) }}</p>
          <div class="question-footer">
            <span class="creator">出题人: {{ question.creatorName || '未知' }}</span>
            <el-button type="primary" size="small">
              开始答题
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      
      <el-empty v-else description="暂无题目" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getQuestions } from '@/api/student'

const router = useRouter()

const loading = ref(false)
const questions = ref([])
const searchKeyword = ref('')
const filterDifficulty = ref('')

const filteredQuestions = computed(() => {
  return questions.value.filter(q => {
    const matchKeyword = !searchKeyword.value || 
      q.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      q.description.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchDifficulty = !filterDifficulty.value || q.difficulty === filterDifficulty.value
    return matchKeyword && matchDifficulty
  })
})

onMounted(() => {
  loadQuestions()
})

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getQuestions()
    if (res.code === 200) {
      questions.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

function goToQuestion(id) {
  router.push(`/student/question/${id}`)
}

function getDifficultyText(difficulty) {
  const map = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return map[difficulty] || '未知'
}

function truncateText(text, maxLength) {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}
</script>

<style lang="scss" scoped>
.questions-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
  
  .filter-bar {
    display: flex;
    gap: 12px;
  }
}

.questions-list {
  min-height: 300px;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
    
    .question-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
      flex: 1;
    }
    
    .question-tags {
      display: flex;
      gap: 8px;
      margin-left: 16px;
    }
  }
  
  .question-desc {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 16px;
  }
  
  .question-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .creator {
      font-size: 13px;
      color: #909399;
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

@media (max-width: 600px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-bar {
    width: 100%;
    
    .el-input, .el-select {
      flex: 1;
    }
  }
  
  .question-header {
    flex-direction: column;
    
    .question-tags {
      margin-left: 0 !important;
      margin-top: 8px;
    }
  }
}
</style>
