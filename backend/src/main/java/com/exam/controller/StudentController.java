package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.dto.SubmissionDTO;
import com.exam.entity.Question;
import com.exam.entity.Submission;
import com.exam.service.QuestionService;
import com.exam.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "学生接口", description = "学生答题、查看成绩等")
public class StudentController {
    
    private final QuestionService questionService;
    private final SubmissionService submissionService;
    
    @GetMapping("/questions")
    @Operation(summary = "获取所有题目列表")
    public ApiResponse<List<Question>> getQuestions() {
        List<Question> questions = questionService.findAll();
        // 隐藏参考答案
        questions.forEach(q -> q.setReferenceAnswer(null));
        return ApiResponse.success(questions);
    }
    
    @GetMapping("/questions/{id}")
    @Operation(summary = "获取题目详情")
    public ApiResponse<Question> getQuestion(@PathVariable Long id) {
        Question question = questionService.findById(id);
        if (question == null) {
            return ApiResponse.error(404, "题目不存在");
        }
        // 隐藏参考答案
        question.setReferenceAnswer(null);
        return ApiResponse.success(question);
    }
    
    @PostMapping("/submit")
    @Operation(summary = "提交答案")
    public ApiResponse<Submission> submitAnswer(@Valid @RequestBody SubmissionDTO dto) {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Submission submission = submissionService.submit(studentId, dto.getQuestionId(), dto.getAnswerCode());
        return ApiResponse.success("提交成功", submission);
    }
    
    @GetMapping("/submissions")
    @Operation(summary = "获取我的提交记录")
    public ApiResponse<List<Submission>> getMySubmissions() {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(submissionService.findByStudent(studentId));
    }
    
    @GetMapping("/submissions/{id}")
    @Operation(summary = "获取提交详情")
    public ApiResponse<Submission> getSubmission(@PathVariable Long id) {
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Submission submission = submissionService.findById(id);
        if (submission == null || !submission.getStudentId().equals(studentId)) {
            return ApiResponse.error(404, "提交记录不存在");
        }
        return ApiResponse.success(submission);
    }
}
