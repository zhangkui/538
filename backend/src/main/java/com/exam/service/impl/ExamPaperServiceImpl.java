package com.exam.service.impl;

import com.exam.dto.ExamPaperDTO;
import com.exam.dto.ExamPaperQuestionDTO;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperQuestion;
import com.exam.entity.Question;
import com.exam.mapper.ExamPaperMapper;
import com.exam.mapper.ExamPaperQuestionMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.service.ExamPaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamPaperServiceImpl implements ExamPaperService {

    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<ExamPaper> findAll() {
        return examPaperMapper.findAll();
    }

    @Override
    public List<ExamPaper> findByTeacher(Long teacherId) {
        return examPaperMapper.findByTeacher(teacherId);
    }

    @Override
    public ExamPaper findById(Long id) {
        ExamPaper paper = examPaperMapper.findById(id);
        if (paper != null) {
            paper.setQuestions(examPaperQuestionMapper.findByPaperId(id));
        }
        return paper;
    }

    @Override
    @Transactional
    public ExamPaper create(Long teacherId, ExamPaperDTO dto) {
        ExamPaper paper = new ExamPaper();
        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setDuration(dto.getDuration() != null ? dto.getDuration() : 120);
        paper.setStartTime(dto.getStartTime());
        paper.setEndTime(dto.getEndTime());
        paper.setPublishStatus("DRAFT");
        paper.setAnswerVisible(dto.getAnswerVisible() != null ? dto.getAnswerVisible() : "AFTER_GRADED");
        paper.setMaxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 1);
        paper.setTargetClasses(dto.getTargetClasses());
        paper.setCreatedBy(teacherId);
        paper.setTotalScore(0);
        examPaperMapper.insert(paper);

        savePaperQuestions(paper.getId(), dto.getQuestions());
        updateTotalScore(paper.getId());

        log.info("创建试卷成功: paperId={}, title={}", paper.getId(), paper.getTitle());
        return findById(paper.getId());
    }

    @Override
    @Transactional
    public ExamPaper update(Long id, ExamPaperDTO dto) {
        ExamPaper paper = examPaperMapper.findById(id);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }
        if ("PUBLISHED".equals(paper.getPublishStatus())) {
            throw new RuntimeException("已发布的试卷不能编辑，请先关闭");
        }

        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setDuration(dto.getDuration() != null ? dto.getDuration() : 120);
        paper.setStartTime(dto.getStartTime());
        paper.setEndTime(dto.getEndTime());
        paper.setAnswerVisible(dto.getAnswerVisible() != null ? dto.getAnswerVisible() : "AFTER_GRADED");
        paper.setMaxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 1);
        paper.setTargetClasses(dto.getTargetClasses());
        examPaperMapper.update(paper);

        examPaperQuestionMapper.deleteByPaperId(id);
        savePaperQuestions(id, dto.getQuestions());
        updateTotalScore(id);

        log.info("更新试卷成功: paperId={}", id);
        return findById(id);
    }

    @Override
    @Transactional
    public ExamPaper copy(Long id, Long teacherId) {
        ExamPaper source = findById(id);
        if (source == null) {
            throw new RuntimeException("源试卷不存在");
        }

        ExamPaper paper = new ExamPaper();
        paper.setTitle(source.getTitle() + "（副本）");
        paper.setDescription(source.getDescription());
        paper.setDuration(source.getDuration());
        paper.setStartTime(source.getStartTime());
        paper.setEndTime(source.getEndTime());
        paper.setPublishStatus("DRAFT");
        paper.setAnswerVisible(source.getAnswerVisible());
        paper.setMaxAttempts(source.getMaxAttempts());
        paper.setTargetClasses(source.getTargetClasses());
        paper.setCreatedBy(teacherId);
        paper.setTotalScore(source.getTotalScore());
        examPaperMapper.insert(paper);

        if (source.getQuestions() != null) {
            for (ExamPaperQuestion srcQ : source.getQuestions()) {
                ExamPaperQuestion newQ = new ExamPaperQuestion();
                newQ.setPaperId(paper.getId());
                newQ.setQuestionId(srcQ.getQuestionId());
                newQ.setQuestionOrder(srcQ.getQuestionOrder());
                newQ.setScore(srcQ.getScore());
                newQ.setTitle(srcQ.getTitle());
                newQ.setDescription(srcQ.getDescription());
                newQ.setRequirements(srcQ.getRequirements());
                newQ.setSampleInput(srcQ.getSampleInput());
                newQ.setSampleOutput(srcQ.getSampleOutput());
                newQ.setReferenceAnswer(srcQ.getReferenceAnswer());
                newQ.setDifficulty(srcQ.getDifficulty());
                newQ.setQuestionType(srcQ.getQuestionType());
                newQ.setOptions(srcQ.getOptions());
                newQ.setCorrectAnswer(srcQ.getCorrectAnswer());
                newQ.setAnswerExplanation(srcQ.getAnswerExplanation());
                examPaperQuestionMapper.insert(newQ);
            }
        }

        log.info("复制试卷成功: sourceId={}, newId={}", id, paper.getId());
        return findById(paper.getId());
    }

    @Override
    public void publish(Long id) {
        ExamPaper paper = examPaperMapper.findById(id);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }
        List<ExamPaperQuestion> questions = examPaperQuestionMapper.findByPaperId(id);
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("试卷没有题目，无法发布");
        }
        examPaperMapper.updateStatus(id, "PUBLISHED");
        log.info("发布试卷成功: paperId={}", id);
    }

    @Override
    public void close(Long id) {
        examPaperMapper.updateStatus(id, "CLOSED");
        log.info("关闭试卷成功: paperId={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        examPaperQuestionMapper.deleteByPaperId(id);
        examPaperMapper.delete(id);
        log.info("删除试卷成功: paperId={}", id);
    }

    private void savePaperQuestions(Long paperId, List<ExamPaperQuestionDTO> questionDTOs) {
        if (questionDTOs == null || questionDTOs.isEmpty()) {
            return;
        }

        int order = 1;
        for (ExamPaperQuestionDTO dto : questionDTOs) {
            ExamPaperQuestion pq = new ExamPaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionOrder(order++);
            pq.setScore(dto.getScore() != null ? dto.getScore() : 0);

            if (dto.getQuestionId() != null) {
                Question q = questionMapper.findById(dto.getQuestionId());
                if (q != null) {
                    pq.setQuestionId(q.getId());
                    pq.setTitle(q.getTitle());
                    pq.setDescription(q.getDescription());
                    pq.setRequirements(q.getRequirements());
                    pq.setSampleInput(q.getSampleInput());
                    pq.setSampleOutput(q.getSampleOutput());
                    pq.setReferenceAnswer(q.getReferenceAnswer());
                    pq.setDifficulty(q.getDifficulty());
                    pq.setQuestionType(q.getQuestionType());
                    pq.setOptions(q.getOptions());
                    pq.setCorrectAnswer(q.getCorrectAnswer());
                    pq.setAnswerExplanation(q.getAnswerExplanation());
                }
            } else {
                pq.setTitle(dto.getTitle());
                pq.setDescription(dto.getDescription());
                pq.setRequirements(dto.getRequirements());
                pq.setSampleInput(dto.getSampleInput());
                pq.setSampleOutput(dto.getSampleOutput());
                pq.setReferenceAnswer(dto.getReferenceAnswer());
                pq.setDifficulty(dto.getDifficulty());
                pq.setQuestionType(dto.getQuestionType());
                pq.setOptions(dto.getOptions());
                pq.setCorrectAnswer(dto.getCorrectAnswer());
                pq.setAnswerExplanation(dto.getAnswerExplanation());
            }

            examPaperQuestionMapper.insert(pq);
        }
    }

    private void updateTotalScore(Long paperId) {
        List<ExamPaperQuestion> questions = examPaperQuestionMapper.findByPaperId(paperId);
        int total = 0;
        if (questions != null) {
            for (ExamPaperQuestion q : questions) {
                total += q.getScore() != null ? q.getScore() : 0;
            }
        }
        ExamPaper paper = examPaperMapper.findById(paperId);
        if (paper != null) {
            paper.setTotalScore(total);
            examPaperMapper.update(paper);
        }
    }
}
