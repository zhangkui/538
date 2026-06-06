import request from '@/utils/request'

export function getAvailableExamPapers() {
  return request({
    url: '/student/exam-papers',
    method: 'get'
  })
}

export function getExamPaperDetail(id) {
  return request({
    url: `/student/exam-papers/${id}`,
    method: 'get'
  })
}

export function getMyPaperSubmission(id) {
  return request({
    url: `/student/exam-papers/${id}/my-submission`,
    method: 'get'
  })
}

export function startExam(id) {
  return request({
    url: `/student/exam-papers/${id}/start`,
    method: 'post'
  })
}

export function saveExamDraft(data) {
  return request({
    url: '/student/exam-papers/save-draft',
    method: 'post',
    data
  })
}

export function submitExamPaper(data) {
  return request({
    url: '/student/exam-papers/submit',
    method: 'post',
    data
  })
}

export function getMyExamSubmissions() {
  return request({
    url: '/student/exam-papers/my-submissions',
    method: 'get'
  })
}
