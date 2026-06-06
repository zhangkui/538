<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <div class="welcome-card">
        <div class="welcome-content">
          <h1>欢迎回来，{{ userStore.realName || userStore.username }}！</h1>
          <p>{{ greeting }}，祝您{{ userStore.isTeacher ? '教学' : '学习' }}愉快！</p>
        </div>
        <div class="welcome-icon">
          <el-icon :size="80"><Sunny /></el-icon>
        </div>
      </div>
    </div>
    
    <!-- 学生统计 -->
    <template v-if="userStore.isStudent">
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8">
            <div class="stat-card stat-primary">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalQuestions }}</h3>
                <p>题目总数</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="stat-card stat-success">
              <div class="stat-icon">
                <el-icon><Finished /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.submitted }}</h3>
                <p>已提交</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="stat-card stat-warning">
              <div class="stat-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.graded }}</h3>
                <p>已批改</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div class="quick-actions">
        <h2 class="section-title">快捷操作</h2>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <div class="action-card" @click="$router.push('/student/questions')">
              <el-icon :size="40" color="#409eff"><Edit /></el-icon>
              <h3>开始答题</h3>
              <p>查看题目列表并提交答案</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12">
            <div class="action-card" @click="$router.push('/student/submissions')">
              <el-icon :size="40" color="#67c23a"><Tickets /></el-icon>
              <h3>查看成绩</h3>
              <p>查看我的提交记录和成绩</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </template>
    
    <!-- 教师统计 -->
    <template v-if="userStore.isTeacher">
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="6">
            <div class="stat-card stat-primary">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalStudents }}</h3>
                <p>学生总数</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="6">
            <div class="stat-card stat-success">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalQuestions }}</h3>
                <p>题目总数</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="6">
            <div class="stat-card stat-warning">
              <div class="stat-icon">
                <el-icon><Tickets /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalSubmissions }}</h3>
                <p>提交总数</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="6">
            <div class="stat-card stat-danger">
              <div class="stat-icon">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.pendingGrade }}</h3>
                <p>待批改</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div class="quick-actions">
        <h2 class="section-title">快捷操作</h2>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8">
            <div class="action-card" @click="$router.push('/teacher/students')">
              <el-icon :size="40" color="#409eff"><UserFilled /></el-icon>
              <h3>学生管理</h3>
              <p>导入和管理学生信息</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="action-card" @click="$router.push('/teacher/questions')">
              <el-icon :size="40" color="#67c23a"><EditPen /></el-icon>
              <h3>题目管理</h3>
              <p>创建和导入题目</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="action-card" @click="$router.push('/teacher/grading')">
              <el-icon :size="40" color="#e6a23c"><Stamp /></el-icon>
              <h3>批改作业</h3>
              <p>查看并批改学生提交</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getQuestions as getStudentQuestions, getMySubmissions } from '@/api/student'
import { getStudents, getQuestions as getTeacherQuestions, getAllSubmissions, getPendingSubmissions } from '@/api/teacher'

const userStore = useUserStore()

const stats = ref({
  totalQuestions: 0,
  submitted: 0,
  graded: 0,
  totalStudents: 0,
  totalSubmissions: 0,
  pendingGrade: 0
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

onMounted(async () => {
  if (userStore.isStudent) {
    await loadStudentStats()
  } else if (userStore.isTeacher) {
    await loadTeacherStats()
  }
})

async function loadStudentStats() {
  try {
    const [questionsRes, submissionsRes] = await Promise.all([
      getStudentQuestions(),
      getMySubmissions()
    ])
    
    if (questionsRes.code === 200) {
      stats.value.totalQuestions = questionsRes.data?.length || 0
    }
    if (submissionsRes.code === 200) {
      const submissions = submissionsRes.data || []
      stats.value.submitted = submissions.length
      stats.value.graded = submissions.filter(s => s.status === 'GRADED').length
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

async function loadTeacherStats() {
  try {
    const [studentsRes, questionsRes, submissionsRes, pendingRes] = await Promise.all([
      getStudents(),
      getTeacherQuestions(),
      getAllSubmissions(),
      getPendingSubmissions()
    ])
    
    if (studentsRes.code === 200) {
      stats.value.totalStudents = studentsRes.data?.length || 0
    }
    if (questionsRes.code === 200) {
      stats.value.totalQuestions = questionsRes.data?.length || 0
    }
    if (submissionsRes.code === 200) {
      stats.value.totalSubmissions = submissionsRes.data?.length || 0
    }
    if (pendingRes.code === 200) {
      stats.value.pendingGrade = pendingRes.data?.length || 0
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 24px;
  
  .welcome-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #fff;
    
    .welcome-content {
      h1 {
        font-size: 28px;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 16px;
        opacity: 0.9;
      }
    }
    
    .welcome-icon {
      opacity: 0.8;
    }
  }
}

.stats-section {
  margin-bottom: 24px;
  
  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    margin-bottom: 16px;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-right: 20px;
      
      .el-icon {
        font-size: 28px;
      }
    }
    
    &.stat-primary .stat-icon {
      background: rgba(64, 158, 255, 0.1);
      .el-icon { color: #409eff; }
    }
    
    &.stat-success .stat-icon {
      background: rgba(103, 194, 58, 0.1);
      .el-icon { color: #67c23a; }
    }
    
    &.stat-warning .stat-icon {
      background: rgba(230, 162, 60, 0.1);
      .el-icon { color: #e6a23c; }
    }
    
    &.stat-danger .stat-icon {
      background: rgba(245, 108, 108, 0.1);
      .el-icon { color: #f56c6c; }
    }
    
    .stat-info {
      h3 {
        font-size: 32px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }
      
      p {
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.quick-actions {
  .action-card {
    background: #fff;
    border-radius: 12px;
    padding: 32px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    margin-bottom: 16px;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    
    h3 {
      font-size: 18px;
      color: #303133;
      margin: 16px 0 8px;
    }
    
    p {
      font-size: 14px;
      color: #909399;
    }
  }
}

@media (max-width: 768px) {
  .welcome-card {
    flex-direction: column;
    text-align: center;
    
    .welcome-icon {
      margin-top: 20px;
    }
  }
}
</style>
