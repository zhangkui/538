package com.exam.service;

import com.exam.dto.ExamGradeDTO;
import com.exam.dto.ExamSubmitDTO;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperSubmission;
import java.util.List;

public interface ExamSubmissionService {
    List<ExamPaperSubmission> findAll();
    List<ExamPaperSubmission> findByPaperId(Long paperId);
    List<ExamPaperSubmission> findByStudentId(Long studentId);
    List<ExamPaper> findPublishedPapersForStudent(Long studentId, String className);
    ExamPaperSubmission findById(Long id);
    ExamPaperSubmission getStudentPaperSubmission(Long studentId, Long paperId);
    ExamPaperSubmission startExam(Long studentId, Long paperId);
    ExamPaperSubmission saveDraft(Long studentId, ExamSubmitDTO dto);
    ExamPaperSubmission submit(Long studentId, ExamSubmitDTO dto);
    ExamPaperSubmission grade(Long submissionId, Long graderId, ExamGradeDTO dto);
}
