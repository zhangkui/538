package com.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionDTO {
    private Long id;
    
    @NotBlank(message = "题目标题不能为空")
    private String title;
    
    @NotBlank(message = "题目描述不能为空")
    private String description;
    
    private String requirements;
    private String sampleInput;
    private String sampleOutput;
    private String referenceAnswer;
    
    @NotNull(message = "分值不能为空")
    private Integer score;
    
    private String difficulty;
    
    @NotBlank(message = "题型不能为空")
    private String questionType;
    
    private String options;
    private String correctAnswer;
    private String answerExplanation;
}
