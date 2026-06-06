package com.exam.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeDTO {
    @NotNull(message = "得分不能为空")
    @Min(value = 0, message = "得分不能小于0")
    @Max(value = 100, message = "得分不能大于100")
    private Integer score;
    
    private String feedback;
}
