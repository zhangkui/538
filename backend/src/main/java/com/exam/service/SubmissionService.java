package com.exam.service;

import com.exam.entity.Submission;
import java.util.List;

public interface SubmissionService {
    Submission findById(Long id);
    List<Submission> findByStudent(Long studentId);
    List<Submission> findAll();
    List<Submission> findPending();
    Submission submit(Long studentId, Long questionId, String answerCode);
    Submission grade(Long submissionId, Long graderId, Integer score, String feedback);
}
