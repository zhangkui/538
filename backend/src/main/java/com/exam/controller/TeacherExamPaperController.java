package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamPaperDTO;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperSubmission;
import com.exam.service.ExamPaperService;
import com.exam.service.ExamSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/exam-papers")
@RequiredArgsConstructor
@Tag(name = "教师试卷管理接口", description = "教师管理试卷的增删改查、发布、复制等")
public class TeacherExamPaperController {

    private final ExamPaperService examPaperService;
    private final ExamSubmissionService examSubmissionService;

    @GetMapping
    @Operation(summary = "获取所有试卷列表")
    public ApiResponse<List<ExamPaper>> getAllPapers() {
        Long teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(examPaperService.findByTeacher(teacherId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取试卷详情（含题目）")
    public ApiResponse<ExamPaper> getPaperDetail(@PathVariable Long id) {
        ExamPaper paper = examPaperService.findById(id);
        if (paper == null) {
            return ApiResponse.error(404, "试卷不存在");
        }
        return ApiResponse.success(paper);
    }

    @PostMapping
    @Operation(summary = "新建试卷")
    public ApiResponse<ExamPaper> createPaper(@Valid @RequestBody ExamPaperDTO dto) {
        if (dto.getQuestions() == null || dto.getQuestions().isEmpty()) {
            return ApiResponse.error(400, "请至少添加一道题目");
        }
        Long teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(examPaperService.create(teacherId, dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑试卷")
    public ApiResponse<ExamPaper> updatePaper(@PathVariable Long id, @Valid @RequestBody ExamPaperDTO dto) {
        try {
            return ApiResponse.success(examPaperService.update(id, dto));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "复制试卷")
    public ApiResponse<ExamPaper> copyPaper(@PathVariable Long id) {
        Long teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return ApiResponse.success(examPaperService.copy(id, teacherId));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布试卷")
    public ApiResponse<Void> publishPaper(@PathVariable Long id) {
        try {
            examPaperService.publish(id);
            return ApiResponse.success("发布成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "关闭试卷")
    public ApiResponse<Void> closePaper(@PathVariable Long id) {
        examPaperService.close(id);
        return ApiResponse.success("关闭成功", null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除试卷")
    public ApiResponse<Void> deletePaper(@PathVariable Long id) {
        examPaperService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "获取某试卷的所有答卷")
    public ApiResponse<List<ExamPaperSubmission>> getPaperSubmissions(@PathVariable Long id) {
        return ApiResponse.success(examSubmissionService.findByPaperId(id));
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "获取答卷详情（教师查看）")
    public ApiResponse<ExamPaperSubmission> getSubmissionDetail(@PathVariable Long submissionId) {
        ExamPaperSubmission submission = examSubmissionService.findById(submissionId);
        if (submission == null) {
            return ApiResponse.error(404, "答卷不存在");
        }
        return ApiResponse.success(submission);
    }
}
