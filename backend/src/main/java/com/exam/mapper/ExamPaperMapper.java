package com.exam.mapper;

import com.exam.entity.ExamPaper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamPaperMapper {

    @Select("SELECT p.*, u.real_name as creator_name, " +
            "(SELECT COUNT(*) FROM exam_paper_question q WHERE q.paper_id = p.id) as question_count " +
            "FROM exam_paper p LEFT JOIN user u ON p.created_by = u.id ORDER BY p.created_at DESC")
    List<ExamPaper> findAll();

    @Select("SELECT p.*, u.real_name as creator_name, " +
            "(SELECT COUNT(*) FROM exam_paper_question q WHERE q.paper_id = p.id) as question_count " +
            "FROM exam_paper p LEFT JOIN user u ON p.created_by = u.id " +
            "WHERE p.created_by = #{teacherId} ORDER BY p.created_at DESC")
    List<ExamPaper> findByTeacher(Long teacherId);

    @Select("SELECT p.*, u.real_name as creator_name, " +
            "(SELECT COUNT(*) FROM exam_paper_question q WHERE q.paper_id = p.id) as question_count " +
            "FROM exam_paper p LEFT JOIN user u ON p.created_by = u.id " +
            "WHERE p.id = #{id}")
    ExamPaper findById(Long id);

    @Insert("INSERT INTO exam_paper (title, description, total_score, duration, start_time, end_time, " +
            "publish_status, answer_visible, max_attempts, target_classes, created_by) " +
            "VALUES (#{title}, #{description}, #{totalScore}, #{duration}, #{startTime}, #{endTime}, " +
            "#{publishStatus}, #{answerVisible}, #{maxAttempts}, #{targetClasses}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamPaper paper);

    @Update("UPDATE exam_paper SET title = #{title}, description = #{description}, " +
            "total_score = #{totalScore}, duration = #{duration}, start_time = #{startTime}, " +
            "end_time = #{endTime}, answer_visible = #{answerVisible}, max_attempts = #{maxAttempts}, " +
            "target_classes = #{targetClasses}, updated_at = NOW() WHERE id = #{id}")
    int update(ExamPaper paper);

    @Update("UPDATE exam_paper SET publish_status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM exam_paper WHERE id = #{id}")
    int delete(Long id);
}
