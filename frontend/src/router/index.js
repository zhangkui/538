import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/ChangePassword.vue'),
    meta: { title: '修改密码' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      // 学生路由
      {
        path: 'student/questions',
        name: 'StudentQuestions',
        component: () => import('@/views/student/Questions.vue'),
        meta: { title: '题目列表', role: 'STUDENT' }
      },
      {
        path: 'student/question/:id',
        name: 'StudentQuestion',
        component: () => import('@/views/student/QuestionDetail.vue'),
        meta: { title: '答题', role: 'STUDENT' }
      },
      {
        path: 'student/submissions',
        name: 'StudentSubmissions',
        component: () => import('@/views/student/Submissions.vue'),
        meta: { title: '我的提交', role: 'STUDENT' }
      },
      // 教师路由
      {
        path: 'teacher/students',
        name: 'TeacherStudents',
        component: () => import('@/views/teacher/Students.vue'),
        meta: { title: '学生管理', role: 'TEACHER' }
      },
      {
        path: 'teacher/questions',
        name: 'TeacherQuestions',
        component: () => import('@/views/teacher/Questions.vue'),
        meta: { title: '题目管理', role: 'TEACHER' }
      },
      {
        path: 'teacher/grading',
        name: 'TeacherGrading',
        component: () => import('@/views/teacher/Grading.vue'),
        meta: { title: '批改作业', role: 'TEACHER' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Java编程题系统` : 'Java编程题系统'
  
  // 公开页面直接放行
  if (to.meta.public) {
    if (token && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
    return
  }
  
  // 需要登录的页面
  if (!token) {
    next('/login')
    return
  }
  
  // 检查是否需要修改密码
  const needChangePwd = userStore.needChangePwd
  if (needChangePwd && to.path !== '/change-password') {
    next('/change-password')
    return
  }
  
  // 检查角色权限
  if (to.meta.role && userStore.role !== to.meta.role) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
