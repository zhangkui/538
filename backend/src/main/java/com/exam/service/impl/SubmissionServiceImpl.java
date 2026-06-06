package com.exam.service.impl;

import com.exam.entity.Question;
import com.exam.entity.Submission;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubmissionMapper;
import com.exam.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
    
    private final SubmissionMapper submissionMapper;
    private final QuestionMapper questionMapper;
    
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
    public Submission submit(Long studentId, Long questionId, String answerCode, String selectedAnswer) {
        Question question = questionMapper.findById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        
        String questionType = question.getQuestionType() != null ? question.getQuestionType() : "PROGRAMMING";
        
        if ("PROGRAMMING".equals(questionType)) {
            if (answerCode == null || answerCode.trim().isEmpty()) {
                throw new RuntimeException("请输入答案代码");
            }
        } else {
            if (selectedAnswer == null || selectedAnswer.trim().isEmpty()) {
                throw new RuntimeException("请选择答案");
            }
        }
        
        Submission existing = submissionMapper.findByStudentAndQuestion(studentId, questionId);
        if (existing != null) {
            existing.setAnswerCode(answerCode);
            existing.setSelectedAnswer(selectedAnswer);
            
            if ("PROGRAMMING".equals(questionType)) {
                submissionMapper.updateAnswer(existing);
            } else {
                int score = calculateChoiceScore(question, selectedAnswer);
                existing.setScore(score);
                submissionMapper.updateAnswer(existing);
                submissionMapper.autoGrade(existing);
            }
            log.info("更新提交答案: studentId={}, questionId={}", studentId, questionId);
            return submissionMapper.findById(existing.getId());
        }
        
        Submission submission = new Submission();
        submission.setStudentId(studentId);
        submission.setQuestionId(questionId);
        submission.setAnswerCode(answerCode);
        submission.setSelectedAnswer(selectedAnswer);
        submissionMapper.insert(submission);
        
        if (!"PROGRAMMING".equals(questionType)) {
            int score = calculateChoiceScore(question, selectedAnswer);
            submission.setScore(score);
            submissionMapper.autoGrade(submission);
        }
        
        log.info("新建提交答案: studentId={}, questionId={}", studentId, questionId);
        return submissionMapper.findById(submission.getId());
    }
    
    private int calculateChoiceScore(Question question, String selectedAnswer) {
        if (question.getCorrectAnswer() == null || selectedAnswer == null) {
            return 0;
        }
        
        String correct = question.getCorrectAnswer().trim().toUpperCase();
        String selected = selectedAnswer.trim().toUpperCase();
        
        Set<String> correctSet = new HashSet<>(Arrays.asList(correct.split(",")));
        Set<String> selectedSet = new HashSet<>(Arrays.asList(selected.split(",")));
        
        if (correctSet.equals(selectedSet)) {
            return question.getScore() != null ? question.getScore() : 0;
        }
        return 0;
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
