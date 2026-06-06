<template>
  <div class="change-password-container">
    <div class="change-password-card">
      <div class="card-header">
        <el-icon :size="48" color="#e6a23c"><Warning /></el-icon>
        <h2>首次登录请修改密码</h2>
        <p>为了您的账号安全，请设置一个新密码</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="password-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            确认修改
          </el-button>
          <el-button @click="handleLogout">返回登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      userStore.setNeedChangePwd(false)
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  userStore.logout()
}
</script>

<style lang="scss" scoped>
.change-password-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.change-password-card {
  width: 500px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
  
  h2 {
    font-size: 22px;
    color: #303133;
    margin: 16px 0 8px;
  }
  
  p {
    color: #909399;
    font-size: 14px;
  }
}

.password-form {
  .el-form-item {
    margin-bottom: 24px;
  }
}

@media (max-width: 560px) {
  .change-password-card {
    width: 90%;
    padding: 30px 20px;
  }
  
  .password-form {
    :deep(.el-form-item__label) {
      width: 80px !important;
    }
  }
}
</style>
