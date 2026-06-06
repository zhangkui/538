package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamAnswer {
    private Long id;
    private Long submissionId;
    private Long paperQuestionId;
    private String answerCode;
    private String selectedAnswer;
    private Integer score;
    private String feedback;
    private Boolean isAutoGraded;
    private Boolean isManualGraded;
    private Long gradedBy;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String graderName;
    private ExamPaperQuestion question;
}
