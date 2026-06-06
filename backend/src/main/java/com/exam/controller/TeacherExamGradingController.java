package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamGradeDTO;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperSubmission;
import com.exam.entity.User;
import com.exam.service.ExamSubmissionService;
import com.exam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/exam-grading")
@RequiredArgsConstructor
@Tag(name = "教师阅卷接口", description = "教师批改试卷、查看答卷等")
public class TeacherExamGradingController {

    private final ExamSubmissionService examSubmissionService;

    @PostMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "批改答卷（给编程题打分）")
    public ApiResponse<ExamPaperSubmission> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ExamGradeDTO dto) {
        Long teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return ApiResponse.success(examSubmissionService.grade(submissionId, teacherId, dto));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/submissions/pending")
    @Operation(summary = "获取待批改的答卷")
    public ApiResponse<List<ExamPaperSubmission>> getPendingSubmissions() {
        List<ExamPaperSubmission> all = examSubmissionService.findAll();
        List<ExamPaperSubmission> pending = all.stream()
                .filter(s -> "PENDING_GRADING".equals(s.getStatus()))
                .toList();
        return ApiResponse.success(pending);
    }
}
