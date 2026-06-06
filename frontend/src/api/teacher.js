import request from '@/utils/request'

// ========== 学生管理 ==========

// 获取所有学生
export function getStudents() {
  return request({
    url: '/teacher/students',
    method: 'get'
  })
}

// 批量导入学生
export function importStudents(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/teacher/students/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除学生
export function deleteStudent(id) {
  return request({
    url: `/teacher/students/${id}`,
    method: 'delete'
  })
}

// ========== 题目管理 ==========

// 获取所有题目
export function getQuestions() {
  return request({
    url: '/teacher/questions',
    method: 'get'
  })
}

// 创建题目
export function createQuestion(data) {
  return request({
    url: '/teacher/questions',
    method: 'post',
    data
  })
}

// 批量导入题目
export function importQuestions(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/teacher/questions/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新题目
export function updateQuestion(id, data) {
  return request({
    url: `/teacher/questions/${id}`,
    method: 'put',
    data
  })
}

// 删除题目
export function deleteQuestion(id) {
  return request({
    url: `/teacher/questions/${id}`,
    method: 'delete'
  })
}

// ========== 批改管理 ==========

// 获取所有提交
export function getAllSubmissions() {
  return request({
    url: '/teacher/submissions',
    method: 'get'
  })
}

// 获取待批改的提交
export function getPendingSubmissions() {
  return request({
    url: '/teacher/submissions/pending',
    method: 'get'
  })
}

// 获取提交详情
export function getSubmission(id) {
  return request({
    url: `/teacher/submissions/${id}`,
    method: 'get'
  })
}

// 批改提交
export function gradeSubmission(id, data) {
  return request({
    url: `/teacher/submissions/${id}/grade`,
    method: 'post',
    data
  })
}
