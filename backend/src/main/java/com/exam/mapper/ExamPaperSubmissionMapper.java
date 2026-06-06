package com.exam.mapper;

import com.exam.entity.ExamPaperSubmission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamPaperSubmissionMapper {

    @Select("SELECT s.*, u.real_name as student_name, u.class_name as class_name, " +
            "p.title as paper_title, p.total_score as paper_total_score, " +
            "u2.real_name as grader_name " +
            "FROM exam_paper_submission s " +
            "LEFT JOIN user u ON s.student_id = u.id " +
            "LEFT JOIN exam_paper p ON s.paper_id = p.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "ORDER BY s.created_at DESC")
    List<ExamPaperSubmission> findAll();

    @Select("SELECT s.*, u.real_name as student_name, u.class_name as class_name, " +
            "p.title as paper_title, p.total_score as paper_total_score, " +
            "u2.real_name as grader_name " +
            "FROM exam_paper_submission s " +
            "LEFT JOIN user u ON s.student_id = u.id " +
            "LEFT JOIN exam_paper p ON s.paper_id = p.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "WHERE s.paper_id = #{paperId} ORDER BY s.created_at DESC")
    List<ExamPaperSubmission> findByPaperId(Long paperId);

    @Select("SELECT s.*, u.real_name as student_name, u.class_name as class_name, " +
            "p.title as paper_title, p.total_score as paper_total_score, " +
            "u2.real_name as grader_name " +
            "FROM exam_paper_submission s " +
            "LEFT JOIN user u ON s.student_id = u.id " +
            "LEFT JOIN exam_paper p ON s.paper_id = p.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "WHERE s.student_id = #{studentId} ORDER BY s.created_at DESC")
    List<ExamPaperSubmission> findByStudentId(Long studentId);

    @Select("SELECT s.*, u.real_name as student_name, u.class_name as class_name, " +
            "p.title as paper_title, p.total_score as paper_total_score, " +
            "u2.real_name as grader_name " +
            "FROM exam_paper_submission s " +
            "LEFT JOIN user u ON s.student_id = u.id " +
            "LEFT JOIN exam_paper p ON s.paper_id = p.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "WHERE s.student_id = #{studentId} AND s.paper_id = #{paperId} " +
            "ORDER BY s.attempt_number DESC LIMIT 1")
    ExamPaperSubmission findByStudentAndPaper(@Param("studentId") Long studentId, @Param("paperId") Long paperId);

    @Select("SELECT s.*, u.real_name as student_name, u.class_name as class_name, " +
            "p.title as paper_title, p.total_score as paper_total_score, " +
            "u2.real_name as grader_name " +
            "FROM exam_paper_submission s " +
            "LEFT JOIN user u ON s.student_id = u.id " +
            "LEFT JOIN exam_paper p ON s.paper_id = p.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "WHERE s.id = #{id}")
    ExamPaperSubmission findById(Long id);

    @Select("SELECT COUNT(*) FROM exam_paper_submission WHERE student_id = #{studentId} AND paper_id = #{paperId}")
    int countAttempts(@Param("studentId") Long studentId, @Param("paperId") Long paperId);

    @Insert("INSERT INTO exam_paper_submission (paper_id, student_id, attempt_number, status) " +
            "VALUES (#{paperId}, #{studentId}, #{attemptNumber}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamPaperSubmission submission);

    @Update("UPDATE exam_paper_submission SET total_score = #{totalScore}, auto_score = #{autoScore}, " +
            "manual_score = #{manualScore}, status = #{status}, submit_time = #{submitTime}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(ExamPaperSubmission submission);

    @Update("UPDATE exam_paper_submission SET total_score = #{totalScore}, manual_score = #{manualScore}, " +
            "status = 'GRADED', graded_by = #{gradedBy}, graded_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    int grade(ExamPaperSubmission submission);

    @Delete("DELETE FROM exam_paper_submission WHERE id = #{id}")
    int delete(Long id);
}
