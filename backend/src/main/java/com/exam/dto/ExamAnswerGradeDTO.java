package com.exam.dto;

import lombok.Data;

@Data
public class ExamAnswerGradeDTO {
    private Long answerId;
    private Integer score;
    private String feedback;
}
