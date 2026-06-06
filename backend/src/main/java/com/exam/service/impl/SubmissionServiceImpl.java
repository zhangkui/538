package com.exam.service.impl;

import com.exam.entity.Submission;
import com.exam.mapper.SubmissionMapper;
import com.exam.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
    
    private final SubmissionMapper submissionMapper;
    
    @Override
    public Submission findById(Long id) {
        return submissionMapper.findById(id);
    }
    
    @Override
    public List<Submission> findByStudent(Long studentId) {
        return submissionMapper.findByStudent(studentId);
    }
    
    @Override
    public List<Submission> findAll() {
        return submissionMapper.findAll();
    }
    
    @Override
    public List<Submission> findPending() {
        return submissionMapper.findByStatus("PENDING");
    }
    
    @Override
    @Transactional
    public Submission submit(Long studentId, Long questionId, String answerCode) {
        // 检查是否已经提交过
        Submission existing = submissionMapper.findByStudentAndQuestion(studentId, questionId);
        if (existing != null) {
            // 更新答案
            existing.setAnswerCode(answerCode);
            submissionMapper.updateAnswer(existing);
            log.info("更新提交答案: studentId={}, questionId={}", studentId, questionId);
            return submissionMapper.findById(existing.getId());
        }
        
        // 新建提交
        Submission submission = new Submission();
        submission.setStudentId(studentId);
        submission.setQuestionId(questionId);
        submission.setAnswerCode(answerCode);
        submissionMapper.insert(submission);
        log.info("新建提交答案: studentId={}, questionId={}", studentId, questionId);
        return submissionMapper.findById(submission.getId());
    }
    
    @Override
    @Transactional
    public Submission grade(Long submissionId, Long graderId, Integer score, String feedback) {
        Submission submission = submissionMapper.findById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setGradedBy(graderId);
        submissionMapper.grade(submission);
        log.info("批改答案完成: submissionId={}, score={}", submissionId, score);
        return submissionMapper.findById(submissionId);
    }
}
