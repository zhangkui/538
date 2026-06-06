package com.exam.dto;

import lombok.Data;

@Data
public class ExamPaperQuestionDTO {
    private Long id;
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
}
