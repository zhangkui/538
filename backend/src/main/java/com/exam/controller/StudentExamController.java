package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamSubmitDTO;
import com.exam.entity.ExamPaper;
import com.exam.entity.ExamPaperSubmission;
import com.exam.entity.ExamPaperQuestion;
import com.exam.entity.User;
import com.exam.service.ExamPaperService;
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
@RequestMapping("/student/exam-papers")
@RequiredArgsConstructor
@Tag(name = "学生试卷接口", description = "学生查看试卷、答题、提交等")
public class StudentExamController {

    private final ExamPaperService examPaperService;
    private final ExamSubmissionService examSubmissionService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "获取我可以参加的试卷列表")
    public ApiResponse<List<ExamPaper>> getAvailablePapers() {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User student = userService.findById(studentId);
        String className = student != null ? student.getClassName() : null;
        List<ExamPaper> papers = examSubmissionService.findPublishedPapersForStudent(studentId, className);
        for (ExamPaper p : papers) {
            p.setQuestions(null);
        }
        return ApiResponse.success(papers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取试卷详情（用于作答）")
    public ApiResponse<ExamPaper> getPaperDetail(@PathVariable Long id) {
        ExamPaper paper = examPaperService.findById(id);
        if (paper == null) {
            return ApiResponse.error(404, "试卷不存在");
        }
        if (paper.getQuestions() != null) {
            for (ExamPaperQuestion q : paper.getQuestions()) {
                q.setCorrectAnswer(null);
                q.setAnswerExplanation(null);
                q.setReferenceAnswer(null);
            }
        }
        return ApiResponse.success(paper);
    }

    @GetMapping("/{id}/my-submission")
    @Operation(summary = "获取我对某试卷的答卷")
    public ApiResponse<ExamPaperSubmission> getMySubmission(@PathVariable Long id) {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ExamPaperSubmission submission = examSubmissionService.getStudentPaperSubmission(studentId, id);
        return ApiResponse.success(submission);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始考试/进入作答")
    public ApiResponse<ExamPaperSubmission> startExam(@PathVariable Long id) {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            ExamPaperSubmission submission = examSubmissionService.startExam(studentId, id);
            if (submission.getAnswers() != null) {
                for (var a : submission.getAnswers()) {
                    if (a.getQuestion() != null) {
                        a.getQuestion().setCorrectAnswer(null);
                        a.getQuestion().setAnswerExplanation(null);
                        a.getQuestion().setReferenceAnswer(null);
                    }
                }
            }
            return ApiResponse.success(submission);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/save-draft")
    @Operation(summary = "保存草稿")
    public ApiResponse<ExamPaperSubmission> saveDraft(@Valid @RequestBody ExamSubmitDTO dto) {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return ApiResponse.success(examSubmissionService.saveDraft(studentId, dto));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/submit")
    @Operation(summary = "提交试卷")
    public ApiResponse<ExamPaperSubmission> submitPaper(@Valid @RequestBody ExamSubmitDTO dto) {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return ApiResponse.success(examSubmissionService.submit(studentId, dto));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/my-submissions")
    @Operation(summary = "获取我的所有答卷记录")
    public ApiResponse<List<ExamPaperSubmission>> getMySubmissions() {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(examSubmissionService.findByStudentId(studentId));
    }
}
