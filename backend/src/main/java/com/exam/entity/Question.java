package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String sampleInput;
    private String sampleOutput;
    private String referenceAnswer;
    private Integer score;
    private String difficulty;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 额外字段
    private String creatorName;
}
