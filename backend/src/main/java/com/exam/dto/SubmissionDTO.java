package com.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionDTO {
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    @NotBlank(message = "答案代码不能为空")
    private String answerCode;
}
