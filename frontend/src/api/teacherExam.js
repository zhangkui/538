import request from '@/utils/request'

// ========== 教师试卷管理 ==========

export function getExamPapers() {
  return request({
    url: '/teacher/exam-papers',
    method: 'get'
  })
}

export function getExamPaperDetail(id) {
  return request({
    url: `/teacher/exam-papers/${id}`,
    method: 'get'
  })
}

export function createExamPaper(data) {
  return request({
    url: '/teacher/exam-papers',
    method: 'post',
    data
  })
}

export function updateExamPaper(id, data) {
  return request({
    url: `/teacher/exam-papers/${id}`,
    method: 'put',
    data
  })
}

export function copyExamPaper(id) {
  return request({
    url: `/teacher/exam-papers/${id}/copy`,
    method: 'post'
  })
}

export function publishExamPaper(id) {
  return request({
    url: `/teacher/exam-papers/${id}/publish`,
    method: 'post'
  })
}

export function closeExamPaper(id) {
  return request({
    url: `/teacher/exam-papers/${id}/close`,
    method: 'post'
  })
}

export function deleteExamPaper(id) {
  return request({
    url: `/teacher/exam-papers/${id}`,
    method: 'delete'
  })
}

// ========== 教师查看答卷 ==========

export function getPaperSubmissions(paperId) {
  return request({
    url: `/teacher/exam-papers/${paperId}/submissions`,
    method: 'get'
  })
}

export function getSubmissionDetail(submissionId) {
  return request({
    url: `/teacher/exam-papers/submissions/${submissionId}`,
    method: 'get'
  })
}

// ========== 教师阅卷 ==========

export function getPendingExamSubmissions() {
  return request({
    url: '/teacher/exam-grading/submissions/pending',
    method: 'get'
  })
}

export function gradeExamSubmission(submissionId, data) {
  return request({
    url: `/teacher/exam-grading/submissions/${submissionId}/grade`,
    method: 'post',
    data
  })
}
