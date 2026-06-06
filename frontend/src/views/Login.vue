<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="48" color="#409eff"><EditPen /></el-icon>
        <h1>Java编程题判断系统</h1>
        <p>请登录您的账号</p>
      </div>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名/学号"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <div class="demo-accounts">
          <p>测试账号</p>
          <div class="account-tags">
            <el-tag type="danger" @click="fillAccount('teacher001', '123456')">
              教师: teacher001 / 123456
            </el-tag>
            <el-tag type="success" @click="fillAccount('2024001', '123456')">
              学生: 2024001 / 123456
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

function fillAccount(username, password) {
  form.username = username
  form.password = password
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const success = await userStore.login(form)
    if (success) {
      ElMessage.success('登录成功')
      
      // 检查是否需要修改密码
      if (userStore.needChangePwd) {
        router.push('/change-password')
      } else {
        router.push('/dashboard')
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  
  .bg-shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    
    &.shape-1 {
      width: 600px;
      height: 600px;
      background: #fff;
      top: -200px;
      right: -100px;
      animation: float 6s ease-in-out infinite;
    }
    
    &.shape-2 {
      width: 400px;
      height: 400px;
      background: #fff;
      bottom: -100px;
      left: -50px;
      animation: float 8s ease-in-out infinite reverse;
    }
    
    &.shape-3 {
      width: 200px;
      height: 200px;
      background: #fff;
      top: 50%;
      left: 10%;
      animation: float 5s ease-in-out infinite;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
  
  h1 {
    font-size: 24px;
    color: #303133;
    margin: 16px 0 8px;
    font-weight: 600;
  }
  
  p {
    color: #909399;
    font-size: 14px;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    
    &:hover {
      opacity: 0.9;
    }
  }
}

.login-footer {
  margin-top: 24px;
  
  .demo-accounts {
    text-align: center;
    
    p {
      color: #909399;
      font-size: 12px;
      margin-bottom: 12px;
    }
    
    .account-tags {
      display: flex;
      flex-direction: column;
      gap: 8px;
      
      .el-tag {
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          transform: scale(1.02);
        }
      }
    }
  }
}

// 响应式
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 30px 20px;
  }
}
</style>
