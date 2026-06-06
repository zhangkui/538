package com.exam.dto;

import lombok.Data;

@Data
public class ExamAnswerDTO {
    private Long paperQuestionId;
    private String answerCode;
    private String selectedAnswer;
}
