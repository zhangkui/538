package com.exam.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitDTO {
    private Long paperId;
    private List<ExamAnswerDTO> answers;
}
