import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, getCurrentUser } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(null)
  const username = ref('')
  const realName = ref('')
  const role = ref('')
  const needChangePwd = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const isTeacher = computed(() => role.value === 'TEACHER')
  const isStudent = computed(() => role.value === 'STUDENT')

  // 登录
  async function login(credentials) {
    const res = await apiLogin(credentials)
    if (res.code === 200) {
      const data = res.data
      token.value = data.token
      userId.value = data.userId
      username.value = data.username
      realName.value = data.realName
      role.value = data.role
      needChangePwd.value = data.needChangePwd
      
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('username', data.username)
      localStorage.setItem('realName', data.realName)
      localStorage.setItem('role', data.role)
      localStorage.setItem('needChangePwd', data.needChangePwd)
      
      return true
    }
    return false
  }

  // 登出
  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    realName.value = ''
    role.value = ''
    needChangePwd.value = false
    
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('role')
    localStorage.removeItem('needChangePwd')
    
    router.push('/login')
  }

  // 从localStorage恢复状态
  function restore() {
    token.value = localStorage.getItem('token') || ''
    userId.value = localStorage.getItem('userId')
    username.value = localStorage.getItem('username') || ''
    realName.value = localStorage.getItem('realName') || ''
    role.value = localStorage.getItem('role') || ''
    needChangePwd.value = localStorage.getItem('needChangePwd') === 'true'
  }

  // 更新needChangePwd状态
  function setNeedChangePwd(value) {
    needChangePwd.value = value
    localStorage.setItem('needChangePwd', value)
  }

  // 初始化时恢复状态
  restore()

  return {
    token,
    userId,
    username,
    realName,
    role,
    needChangePwd,
    isLoggedIn,
    isTeacher,
    isStudent,
    login,
    logout,
    restore,
    setNeedChangePwd
  }
})
