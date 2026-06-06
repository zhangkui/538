package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamPaperSubmission {
    private Long id;
    private Long paperId;
    private Long studentId;
    private Integer attemptNumber;
    private Integer totalScore;
    private Integer autoScore;
    private Integer manualScore;
    private String status;
    private LocalDateTime submitTime;
    private Long gradedBy;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String studentName;
    private String className;
    private String paperTitle;
    private String graderName;
    private Integer paperTotalScore;
    private List<ExamAnswer> answers;
}
