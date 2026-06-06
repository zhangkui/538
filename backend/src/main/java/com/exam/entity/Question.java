package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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
    private String questionType;
    private String options;
    private String correctAnswer;
    private String answerExplanation;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String creatorName;
}
