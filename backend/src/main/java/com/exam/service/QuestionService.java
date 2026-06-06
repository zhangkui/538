package com.exam.service;

import com.exam.entity.Question;
import java.util.List;

public interface QuestionService {
    Question findById(Long id);
    List<Question> findAll();
    List<Question> findByCreator(Long createdBy);
    Question create(Question question);
    int batchCreate(List<Question> questions);
    Question update(Question question);
    void delete(Long id);
}
