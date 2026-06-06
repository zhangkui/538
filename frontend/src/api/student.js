import request from '@/utils/request'

// 获取题目列表
export function getQuestions() {
  return request({
    url: '/student/questions',
    method: 'get'
  })
}

// 获取题目详情
export function getQuestion(id) {
  return request({
    url: `/student/questions/${id}`,
    method: 'get'
  })
}

// 提交答案
export function submitAnswer(data) {
  return request({
    url: '/student/submit',
    method: 'post',
    data
  })
}

// 获取我的提交记录
export function getMySubmissions() {
  return request({
    url: '/student/submissions',
    method: 'get'
  })
}

// 获取提交详情
export function getSubmission(id) {
  return request({
    url: `/student/submissions/${id}`,
    method: 'get'
  })
}
