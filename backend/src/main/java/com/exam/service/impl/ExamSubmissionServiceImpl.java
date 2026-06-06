package com.exam.service.impl;

import com.exam.dto.ExamAnswerDTO;
import com.exam.dto.ExamAnswerGradeDTO;
import com.exam.dto.ExamGradeDTO;
import com.exam.dto.ExamSubmitDTO;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.ExamSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSubmissionServiceImpl implements ExamSubmissionService {

    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamPaperSubmissionMapper examPaperSubmissionMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final UserMapper userMapper;

    @Override
    public List<ExamPaperSubmission> findAll() {
        return examPaperSubmissionMapper.findAll();
    }

    @Override
    public List<ExamPaperSubmission> findByPaperId(Long paperId) {
        return examPaperSubmissionMapper.findByPaperId(paperId);
    }

    @Override
    public List<ExamPaperSubmission> findByStudentId(Long studentId) {
        List<ExamPaperSubmission> submissions = examPaperSubmissionMapper.findByStudentId(studentId);
        for (ExamPaperSubmission s : submissions) {
            applyAnswerVisibility(s, studentId);
        }
        return submissions;
    }

    @Override
    public List<ExamPaper> findPublishedPapersForStudent(Long studentId, String className) {
        List<ExamPaper> allPapers = examPaperMapper.findAll();
        List<ExamPaper> result = new ArrayList<>();

        for (ExamPaper paper : allPapers) {
            if (!"PUBLISHED".equals(paper.getPublishStatus())) {
                continue;
            }
            if (paper.getTargetClasses() != null && !paper.getTargetClasses().trim().isEmpty()) {
                List<String> targetClasses = Arrays.asList(paper.getTargetClasses().split(","));
                if (className == null || !targetClasses.contains(className.trim())) {
                    continue;
                }
            }
            result.add(paper);
        }
        return result;
    }

    @Override
    public ExamPaperSubmission findById(Long id) {
        ExamPaperSubmission submission = examPaperSubmissionMapper.findById(id);
        if (submission != null) {
            submission.setAnswers(examAnswerMapper.findBySubmissionId(id));
        }
        return submission;
    }

    @Override
    public ExamPaperSubmission getStudentPaperSubmission(Long studentId, Long paperId) {
        ExamPaperSubmission submission = examPaperSubmissionMapper.findByStudentAndPaper(studentId, paperId);
        if (submission != null) {
            submission.setAnswers(examAnswerMapper.findBySubmissionId(submission.getId()));
            applyAnswerVisibility(submission, studentId);
        }
        return submission;
    }

    @Override
    @Transactional
    public ExamPaperSubmission startExam(Long studentId, Long paperId) {
        ExamPaper paper = examPaperMapper.findById(paperId);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }
        if (!"PUBLISHED".equals(paper.getPublishStatus())) {
            throw new RuntimeException("试卷未发布");
        }
        if (paper.getStartTime() != null && LocalDateTime.now().isBefore(paper.getStartTime())) {
            throw new RuntimeException("考试尚未开始");
        }
        if (paper.getEndTime() != null && LocalDateTime.now().isAfter(paper.getEndTime())) {
            throw new RuntimeException("考试已结束");
        }

        int attempts = examPaperSubmissionMapper.countAttempts(studentId, paperId);
        if (paper.getMaxAttempts() != null && paper.getMaxAttempts() > 0 && attempts >= paper.getMaxAttempts()) {
            throw new RuntimeException("已达到最大作答次数");
        }

        ExamPaperSubmission existing = examPaperSubmissionMapper.findByStudentAndPaper(studentId, paperId);
        if (existing != null && "DRAFT".equals(existing.getStatus())) {
            existing.setAnswers(examAnswerMapper.findBySubmissionId(existing.getId()));
            return existing;
        }

        ExamPaperSubmission submission = new ExamPaperSubmission();
        submission.setPaperId(paperId);
        submission.setStudentId(studentId);
        submission.setAttemptNumber(attempts + 1);
        submission.setStatus("DRAFT");
        submission.setTotalScore(0);
        submission.setAutoScore(0);
        submission.setManualScore(0);
        examPaperSubmissionMapper.insert(submission);

        List<ExamPaperQuestion> questions = examPaperQuestionMapper.findByPaperId(paperId);
        for (ExamPaperQuestion q : questions) {
            ExamAnswer answer = new ExamAnswer();
            answer.setSubmissionId(submission.getId());
            answer.setPaperQuestionId(q.getId());
            answer.setScore(0);
            answer.setIsAutoGraded(false);
            answer.setIsManualGraded(false);
            examAnswerMapper.insert(answer);
        }

        submission.setAnswers(examAnswerMapper.findBySubmissionId(submission.getId()));
        log.info("开始考试: studentId={}, paperId={}, submissionId={}", studentId, paperId, submission.getId());
        return submission;
    }

    @Override
    @Transactional
    public ExamPaperSubmission saveDraft(Long studentId, ExamSubmitDTO dto) {
        ExamPaperSubmission submission = getOrCreateSubmission(studentId, dto.getPaperId());
        if (!"DRAFT".equals(submission.getStatus())) {
            throw new RuntimeException("该试卷已提交，不能再保存草稿");
        }
        updateAnswers(submission.getId(), dto.getAnswers(), false);
        return findById(submission.getId());
    }

    @Override
    @Transactional
    public ExamPaperSubmission submit(Long studentId, ExamSubmitDTO dto) {
        ExamPaperSubmission submission = getOrCreateSubmission(studentId, dto.getPaperId());
        if ("SUBMITTED".equals(submission.getStatus()) || "PENDING_GRADING".equals(submission.getStatus()) || "GRADED".equals(submission.getStatus())) {
            throw new RuntimeException("该试卷已提交，不能重复提交");
        }

        updateAnswers(submission.getId(), dto.getAnswers(), true);

        int autoScore = calculateAutoScore(submission.getId());
        boolean hasProgramming = hasProgrammingQuestions(submission.getId());

        submission.setAutoScore(autoScore);
        submission.setTotalScore(autoScore);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStatus(hasProgramming ? "PENDING_GRADING" : "GRADED");
        examPaperSubmissionMapper.update(submission);

        if (!hasProgramming) {
            submission.setGradedAt(LocalDateTime.now());
            examPaperSubmissionMapper.grade(submission);
        }

        log.info("提交试卷: studentId={}, paperId={}, submissionId={}, autoScore={}",
                studentId, dto.getPaperId(), submission.getId(), autoScore);
        return findById(submission.getId());
    }

    @Override
    @Transactional
    public ExamPaperSubmission grade(Long submissionId, Long graderId, ExamGradeDTO dto) {
        ExamPaperSubmission submission = examPaperSubmissionMapper.findById(submissionId);
        if (submission == null) {
            throw new RuntimeException("答卷不存在");
        }

        int manualScore = 0;
        if (dto.getAnswers() != null) {
            for (ExamAnswerGradeDTO ag : dto.getAnswers()) {
                ExamAnswer answer = examAnswerMapper.findById(ag.getAnswerId());
                if (answer != null) {
                    answer.setScore(ag.getScore());
                    answer.setFeedback(ag.getFeedback());
                    answer.setGradedBy(graderId);
                    answer.setIsManualGraded(true);
                    examAnswerMapper.manualGrade(answer);
                    if (ag.getScore() != null) {
                        manualScore += ag.getScore();
                    }
                }
            }
        }

        List<ExamAnswer> answers = examAnswerMapper.findBySubmissionId(submissionId);
        int autoScore = 0;
        int totalManualScore = 0;
        for (ExamAnswer a : answers) {
            if (Boolean.TRUE.equals(a.getIsAutoGraded()) && a.getScore() != null) {
                autoScore += a.getScore();
            }
            if (Boolean.TRUE.equals(a.getIsManualGraded()) && a.getScore() != null) {
                totalManualScore += a.getScore();
            }
        }

        submission.setAutoScore(autoScore);
        submission.setManualScore(totalManualScore);
        submission.setTotalScore(autoScore + totalManualScore);
        submission.setGradedBy(graderId);
        examPaperSubmissionMapper.grade(submission);

        log.info("批改答卷完成: submissionId={}, totalScore={}", submissionId, submission.getTotalScore());
        return findById(submissionId);
    }

    private ExamPaperSubmission getOrCreateSubmission(Long studentId, Long paperId) {
        ExamPaperSubmission submission = examPaperSubmissionMapper.findByStudentAndPaper(studentId, paperId);
        if (submission == null) {
            return startExam(studentId, paperId);
        }
        return submission;
    }

    private void updateAnswers(Long submissionId, List<ExamAnswerDTO> answerDTOs, boolean isSubmit) {
        if (answerDTOs == null) return;

        for (ExamAnswerDTO dto : answerDTOs) {
            ExamAnswer answer = examAnswerMapper.findBySubmissionAndQuestion(submissionId, dto.getPaperQuestionId());
            if (answer == null) continue;

            answer.setAnswerCode(dto.getAnswerCode());
            answer.setSelectedAnswer(dto.getSelectedAnswer());

            if (isSubmit) {
                ExamPaperQuestion question = examPaperQuestionMapper.findById(dto.getPaperQuestionId());
                if (question != null && !"PROGRAMMING".equals(question.getQuestionType())) {
                    int score = calculateChoiceScore(question, dto.getSelectedAnswer());
                    answer.setScore(score);
                    answer.setIsAutoGraded(true);
                    examAnswerMapper.autoGrade(answer);
                    continue;
                }
            }
            examAnswerMapper.updateAnswer(answer);
        }
    }

    private int calculateChoiceScore(ExamPaperQuestion question, String selectedAnswer) {
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

    private int calculateAutoScore(Long submissionId) {
        List<ExamAnswer> answers = examAnswerMapper.findBySubmissionId(submissionId);
        int score = 0;
        for (ExamAnswer a : answers) {
            if (Boolean.TRUE.equals(a.getIsAutoGraded()) && a.getScore() != null) {
                score += a.getScore();
            }
        }
        return score;
    }

    private boolean hasProgrammingQuestions(Long submissionId) {
        List<ExamAnswer> answers = examAnswerMapper.findBySubmissionId(submissionId);
        for (ExamAnswer a : answers) {
            if (a.getQuestion() != null && "PROGRAMMING".equals(a.getQuestion().getQuestionType())) {
                return true;
            }
        }
        return false;
    }

    private void applyAnswerVisibility(ExamPaperSubmission submission, Long studentId) {
        ExamPaper paper = examPaperMapper.findById(submission.getPaperId());
        if (paper == null) return;

        String visibility = paper.getAnswerVisible() != null ? paper.getAnswerVisible() : "AFTER_GRADED";
        boolean visible = switch (visibility) {
            case "ALWAYS" -> true;
            case "AFTER_SUBMIT" -> !"DRAFT".equals(submission.getStatus());
            case "AFTER_GRADED" -> "GRADED".equals(submission.getStatus());
            case "NEVER" -> false;
            default -> false;
        };

        if (!visible && submission.getAnswers() != null) {
            for (ExamAnswer a : submission.getAnswers()) {
                if (a.getQuestion() != null) {
                    a.getQuestion().setCorrectAnswer(null);
                    a.getQuestion().setAnswerExplanation(null);
                    a.getQuestion().setReferenceAnswer(null);
                }
            }
        }
    }
}
