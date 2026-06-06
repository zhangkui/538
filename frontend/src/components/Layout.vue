<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-container">
        <el-icon :size="28" color="#409eff"><EditPen /></el-icon>
        <span v-if="!isCollapse" class="logo-text">编程题系统</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        class="layout-menu"
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <!-- 学生菜单 -->
        <template v-if="userStore.isStudent">
          <el-menu-item index="/student/questions">
            <el-icon><Document /></el-icon>
            <template #title>题目列表</template>
          </el-menu-item>
          <el-menu-item index="/student/submissions">
            <el-icon><Finished /></el-icon>
            <template #title>我的提交</template>
          </el-menu-item>
        </template>
        
        <!-- 教师菜单 -->
        <template v-if="userStore.isTeacher">
          <el-menu-item index="/teacher/students">
            <el-icon><User /></el-icon>
            <template #title>学生管理</template>
          </el-menu-item>
          <el-menu-item index="/teacher/questions">
            <el-icon><Edit /></el-icon>
            <template #title>题目管理</template>
          </el-menu-item>
          <el-menu-item index="/teacher/grading">
            <el-icon><Stamp /></el-icon>
            <template #title>批改作业</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon 
            class="collapse-btn" 
            @click="toggleCollapse"
            :size="20"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title !== '首页'">
              {{ currentRoute.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <el-tag size="small" :type="userStore.isTeacher ? 'danger' : 'success'">
                {{ userStore.isTeacher ? '教师' : '学生' }}
              </el-tag>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleCommand(command) {
  if (command === 'password') {
    router.push('/change-password')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
    }).catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  width: 100%;
}

.layout-aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
  
  .logo-container {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    .logo-text {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
      margin-left: 10px;
      white-space: nowrap;
    }
  }
  
  .layout-menu {
    border-right: none;
    
    :deep(.el-menu-item) {
      &:hover {
        background-color: rgba(255, 255, 255, 0.05);
      }
      
      &.is-active {
        background-color: #409eff !important;
        color: #fff !important;
      }
    }
  }
}

.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      cursor: pointer;
      margin-right: 16px;
      color: #666;
      transition: color 0.3s;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .user-avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      
      .user-name {
        margin: 0 10px;
        color: #333;
      }
    }
  }
}

.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

// 响应式
@media (max-width: 768px) {
  .layout-aside {
    position: fixed;
    z-index: 100;
    height: 100vh;
  }
  
  .user-name {
    display: none;
  }
}
</style>
