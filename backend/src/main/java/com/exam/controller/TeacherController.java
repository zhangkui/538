package com.exam.controller;

import com.exam.dto.*;
import com.exam.entity.Question;
import com.exam.entity.Submission;
import com.exam.entity.User;
import com.exam.service.QuestionService;
import com.exam.service.SubmissionService;
import com.exam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
@Tag(name = "教师接口", description = "教师管理学生、题目、批改等")
public class TeacherController {
    
    private final UserService userService;
    private final QuestionService questionService;
    private final SubmissionService submissionService;
    
    // ========== 学生管理 ==========
    
    @GetMapping("/students")
    @Operation(summary = "获取所有学生列表")
    public ApiResponse<List<User>> getAllStudents() {
        List<User> students = userService.findAllStudents();
        students.forEach(s -> s.setPassword(null));
        return ApiResponse.success(students);
    }
    
    @PostMapping("/students/import")
    @Operation(summary = "批量导入学生（Excel文件）")
    public ApiResponse<Integer> importStudents(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            List<User> students = new ArrayList<>();
            
            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                User student = new User();
                student.setUsername(getCellValue(row.getCell(0)));  // 学号
                student.setRealName(getCellValue(row.getCell(1)));  // 姓名
                student.setClassName(getCellValue(row.getCell(2))); // 班级
                
                if (student.getUsername() != null && !student.getUsername().isEmpty()) {
                    students.add(student);
                }
            }
            
            if (students.isEmpty()) {
                return ApiResponse.error(400, "Excel文件中没有有效数据");
            }
            
            int count = userService.batchCreateStudents(students);
            return ApiResponse.success("成功导入 " + count + " 名学生", count);
            
        } catch (Exception e) {
            log.error("导入学生失败", e);
            return ApiResponse.error(500, "导入失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/students/{id}")
    @Operation(summary = "删除学生")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }
    
    // ========== 题目管理 ==========
    
    @GetMapping("/questions")
    @Operation(summary = "获取所有题目")
    public ApiResponse<List<Question>> getAllQuestions() {
        return ApiResponse.success(questionService.findAll());
    }
    
    @PostMapping("/questions")
    @Operation(summary = "创建题目")
    public ApiResponse<Question> createQuestion(@Valid @RequestBody QuestionDTO dto) {
        Long teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Question question = new Question();
        question.setTitle(dto.getTitle());
        question.setDescription(dto.getDescription());
        question.setRequirements(dto.getRequirements());
        question.setSampleInput(dto.getSampleInput());
        question.setSampleOutput(dto.getSampleOutput());
        question.setReferenceAnswer(dto.getReferenceAnswer());
        question.setScore(dto.getScore());
        question.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : "MEDIUM");
        question.setCreatedBy(teacherId);
        
        return ApiResponse.success(questionService.create(question));
    }
    
    @PostMapping("/questions/import")
    @Operation(summary = "批量导入题目（Excel文件）")
    public ApiResponse<Integer> importQuestions(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Long teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Sheet sheet = workbook.getSheetAt(0);
            List<Question> questions = new ArrayList<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Question q = new Question();
                q.setTitle(getCellValue(row.getCell(0)));
                q.setDescription(getCellValue(row.getCell(1)));
                q.setRequirements(getCellValue(row.getCell(2)));
                q.setSampleInput(getCellValue(row.getCell(3)));
                q.setSampleOutput(getCellValue(row.getCell(4)));
                q.setReferenceAnswer(getCellValue(row.getCell(5)));
                q.setScore(parseIntOrDefault(getCellValue(row.getCell(6)), 10));
                q.setDifficulty(getCellValue(row.getCell(7)));
                q.setCreatedBy(teacherId);
                
                if (q.getTitle() != null && !q.getTitle().isEmpty()) {
                    questions.add(q);
                }
            }
            
            if (questions.isEmpty()) {
                return ApiResponse.error(400, "Excel文件中没有有效数据");
            }
            
            int count = questionService.batchCreate(questions);
            return ApiResponse.success("成功导入 " + count + " 道题目", count);
            
        } catch (Exception e) {
            log.error("导入题目失败", e);
            return ApiResponse.error(500, "导入失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/questions/{id}")
    @Operation(summary = "更新题目")
    public ApiResponse<Question> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        Question question = questionService.findById(id);
        if (question == null) {
            return ApiResponse.error(404, "题目不存在");
        }
        
        question.setTitle(dto.getTitle());
        question.setDescription(dto.getDescription());
        question.setRequirements(dto.getRequirements());
        question.setSampleInput(dto.getSampleInput());
        question.setSampleOutput(dto.getSampleOutput());
        question.setReferenceAnswer(dto.getReferenceAnswer());
        question.setScore(dto.getScore());
        question.setDifficulty(dto.getDifficulty());
        
        return ApiResponse.success(questionService.update(question));
    }
    
    @DeleteMapping("/questions/{id}")
    @Operation(summary = "删除题目")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        questionService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    // ========== 批改管理 ==========
    
    @GetMapping("/submissions")
    @Operation(summary = "获取所有提交记录")
    public ApiResponse<List<Submission>> getAllSubmissions() {
        return ApiResponse.success(submissionService.findAll());
    }
    
    @GetMapping("/submissions/pending")
    @Operation(summary = "获取待批改的提交")
    public ApiResponse<List<Submission>> getPendingSubmissions() {
        return ApiResponse.success(submissionService.findPending());
    }
    
    @GetMapping("/submissions/{id}")
    @Operation(summary = "获取提交详情")
    public ApiResponse<Submission> getSubmission(@PathVariable Long id) {
        Submission submission = submissionService.findById(id);
        if (submission == null) {
            return ApiResponse.error(404, "提交记录不存在");
        }
        return ApiResponse.success(submission);
    }
    
    @PostMapping("/submissions/{id}/grade")
    @Operation(summary = "批改提交")
    public ApiResponse<Submission> gradeSubmission(@PathVariable Long id, @Valid @RequestBody GradeDTO dto) {
        Long teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.success(submissionService.grade(id, teacherId, dto.getScore(), dto.getFeedback()));
    }
    
    // ========== 工具方法 ==========
    
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> null;
        };
    }
    
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
