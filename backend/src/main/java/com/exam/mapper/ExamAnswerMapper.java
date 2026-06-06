package com.exam.mapper;

import com.exam.entity.ExamAnswer;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamAnswerMapper {

    @Select("SELECT a.*, u.real_name as grader_name, q.* " +
            "FROM exam_answer a " +
            "LEFT JOIN user u ON a.graded_by = u.id " +
            "LEFT JOIN exam_paper_question q ON a.paper_question_id = q.id " +
            "WHERE a.submission_id = #{submissionId} " +
            "ORDER BY q.question_order ASC, q.id ASC")
    List<ExamAnswer> findBySubmissionId(Long submissionId);

    @Select("SELECT a.*, u.real_name as grader_name, q.* " +
            "FROM exam_answer a " +
            "LEFT JOIN user u ON a.graded_by = u.id " +
            "LEFT JOIN exam_paper_question q ON a.paper_question_id = q.id " +
            "WHERE a.id = #{id}")
    ExamAnswer findById(Long id);

    @Select("SELECT * FROM exam_answer WHERE submission_id = #{submissionId} AND paper_question_id = #{paperQuestionId}")
    ExamAnswer findBySubmissionAndQuestion(@Param("submissionId") Long submissionId, @Param("paperQuestionId") Long paperQuestionId);

    @Insert("INSERT INTO exam_answer (submission_id, paper_question_id, answer_code, selected_answer) " +
            "VALUES (#{submissionId}, #{paperQuestionId}, #{answerCode}, #{selectedAnswer})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamAnswer answer);

    @Update("UPDATE exam_answer SET answer_code = #{answerCode}, selected_answer = #{selectedAnswer}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateAnswer(ExamAnswer answer);

    @Update("UPDATE exam_answer SET score = #{score}, is_auto_graded = #{isAutoGraded}, " +
            "graded_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    int autoGrade(ExamAnswer answer);

    @Update("UPDATE exam_answer SET score = #{score}, feedback = #{feedback}, " +
            "is_manual_graded = 1, graded_by = #{gradedBy}, graded_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    int manualGrade(ExamAnswer answer);

    @Delete("DELETE FROM exam_answer WHERE submission_id = #{submissionId}")
    int deleteBySubmissionId(Long submissionId);
}
