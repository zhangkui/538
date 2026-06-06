package com.exam.service.impl;

import com.exam.entity.Question;
import com.exam.mapper.QuestionMapper;
import com.exam.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    
    private final QuestionMapper questionMapper;
    
    @Override
    public Question findById(Long id) {
        return questionMapper.findById(id);
    }
    
    @Override
    public List<Question> findAll() {
        return questionMapper.findAll();
    }
    
    @Override
    public List<Question> findByCreator(Long createdBy) {
        return questionMapper.findByCreator(createdBy);
    }
    
    @Override
    @Transactional
    public Question create(Question question) {
        questionMapper.insert(question);
        log.info("创建题目成功: {}", question.getTitle());
        return question;
    }
    
    @Override
    @Transactional
    public int batchCreate(List<Question> questions) {
        int count = questionMapper.batchInsert(questions);
        log.info("批量创建题目成功，数量: {}", count);
        return count;
    }
    
    @Override
    @Transactional
    public Question update(Question question) {
        questionMapper.update(question);
        log.info("更新题目成功: {}", question.getId());
        return questionMapper.findById(question.getId());
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        questionMapper.delete(id);
        log.info("删除题目成功: {}", id);
    }
}
