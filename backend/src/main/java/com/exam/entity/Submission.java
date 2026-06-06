package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Submission {
    private Long id;
    private Long studentId;
    private Long questionId;
    private String answerCode;
    private String selectedAnswer;
    private Integer score;
    private String feedback;
    private String status;
    private Long gradedBy;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String studentName;
    private String questionTitle;
    private String graderName;
    private String questionType;
    private String correctAnswer;
    private String answerExplanation;
}
