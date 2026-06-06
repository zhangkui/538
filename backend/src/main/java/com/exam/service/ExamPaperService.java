package com.exam.service;

import com.exam.dto.ExamPaperDTO;
import com.exam.entity.ExamPaper;
import java.util.List;

public interface ExamPaperService {
    List<ExamPaper> findAll();
    List<ExamPaper> findByTeacher(Long teacherId);
    ExamPaper findById(Long id);
    ExamPaper create(Long teacherId, ExamPaperDTO dto);
    ExamPaper update(Long id, ExamPaperDTO dto);
    ExamPaper copy(Long id, Long teacherId);
    void publish(Long id);
    void close(Long id);
    void delete(Long id);
}
