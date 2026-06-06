package com.exam.mapper;

import com.exam.entity.Submission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SubmissionMapper {
    
    @Select("SELECT s.*, u1.real_name as student_name, q.title as question_title, " +
            "u2.real_name as grader_name FROM submission s " +
            "LEFT JOIN user u1 ON s.student_id = u1.id " +
            "LEFT JOIN question q ON s.question_id = q.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id WHERE s.id = #{id}")
    Submission findById(Long id);
    
    @Select("SELECT s.*, u1.real_name as student_name, q.title as question_title, " +
            "u2.real_name as grader_name FROM submission s " +
            "LEFT JOIN user u1 ON s.student_id = u1.id " +
            "LEFT JOIN question q ON s.question_id = q.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "WHERE s.student_id = #{studentId} ORDER BY s.created_at DESC")
    List<Submission> findByStudent(Long studentId);
    
    @Select("SELECT s.*, u1.real_name as student_name, q.title as question_title, " +
            "u2.real_name as grader_name FROM submission s " +
            "LEFT JOIN user u1 ON s.student_id = u1.id " +
            "LEFT JOIN question q ON s.question_id = q.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "ORDER BY s.created_at DESC")
    List<Submission> findAll();
    
    @Select("SELECT s.*, u1.real_name as student_name, q.title as question_title, " +
            "u2.real_name as grader_name FROM submission s " +
            "LEFT JOIN user u1 ON s.student_id = u1.id " +
            "LEFT JOIN question q ON s.question_id = q.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "WHERE s.status = #{status} ORDER BY s.created_at DESC")
    List<Submission> findByStatus(String status);
    
    @Select("SELECT s.*, u1.real_name as student_name, q.title as question_title, " +
            "u2.real_name as grader_name FROM submission s " +
            "LEFT JOIN user u1 ON s.student_id = u1.id " +
            "LEFT JOIN question q ON s.question_id = q.id " +
            "LEFT JOIN user u2 ON s.graded_by = u2.id " +
            "WHERE s.student_id = #{studentId} AND s.question_id = #{questionId}")
    Submission findByStudentAndQuestion(@Param("studentId") Long studentId, 
                                         @Param("questionId") Long questionId);
    
    @Insert("INSERT INTO submission (student_id, question_id, answer_code) " +
            "VALUES (#{studentId}, #{questionId}, #{answerCode})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Submission submission);
    
    @Update("UPDATE submission SET answer_code = #{answerCode}, status = 'PENDING', " +
            "score = NULL, feedback = NULL, graded_by = NULL, graded_at = NULL, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateAnswer(Submission submission);
    
    @Update("UPDATE submission SET score = #{score}, feedback = #{feedback}, status = 'GRADED', " +
            "graded_by = #{gradedBy}, graded_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    int grade(Submission submission);
    
    @Delete("DELETE FROM submission WHERE id = #{id}")
    int delete(Long id);
}
