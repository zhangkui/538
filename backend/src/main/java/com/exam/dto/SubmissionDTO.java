package com.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionDTO {
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    private String answerCode;
    private String selectedAnswer;
}
