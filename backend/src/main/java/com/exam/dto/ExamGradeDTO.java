package com.exam.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamGradeDTO {
    private List<ExamAnswerGradeDTO> answers;
    private String feedback;
}
