package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamPaperQuestion {
    private Long id;
    private Long paperId;
    private Long questionId;
    private Integer questionOrder;
    private Integer score;
    private String title;
    private String description;
    private String requirements;
    private String sampleInput;
    private String sampleOutput;
    private String referenceAnswer;
    private String difficulty;
    private String questionType;
    private String options;
    private String correctAnswer;
    private String answerExplanation;
    private LocalDateTime createdAt;
}
