package com.exam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamPaperDTO {
    private Long id;

    @NotBlank(message = "试卷标题不能为空")
    private String title;

    private String description;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String answerVisible;
    private Integer maxAttempts;
    private String targetClasses;
    private List<ExamPaperQuestionDTO> questions;
}
