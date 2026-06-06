package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamPaper {
    private Long id;
    private String title;
    private String description;
    private Integer totalScore;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String publishStatus;
    private String answerVisible;
    private Integer maxAttempts;
    private String targetClasses;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String creatorName;
    private List<ExamPaperQuestion> questions;
    private Integer questionCount;
}
