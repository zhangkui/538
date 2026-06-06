package com.exam.mapper;

import com.exam.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamPaperQuestionMapper {

    @Select("SELECT * FROM exam_paper_question WHERE paper_id = #{paperId} ORDER BY question_order ASC, id ASC")
    List<ExamPaperQuestion> findByPaperId(Long paperId);

    @Select("SELECT * FROM exam_paper_question WHERE id = #{id}")
    ExamPaperQuestion findById(Long id);

    @Insert("INSERT INTO exam_paper_question (paper_id, question_id, question_order, score, title, description, " +
            "requirements, sample_input, sample_output, reference_answer, difficulty, question_type, " +
            "options, correct_answer, answer_explanation) " +
            "VALUES (#{paperId}, #{questionId}, #{questionOrder}, #{score}, #{title}, #{description}, " +
            "#{requirements}, #{sampleInput}, #{sampleOutput}, #{referenceAnswer}, #{difficulty}, #{questionType}, " +
            "#{options}, #{correctAnswer}, #{answerExplanation})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamPaperQuestion question);

    @Delete("DELETE FROM exam_paper_question WHERE paper_id = #{paperId}")
    int deleteByPaperId(Long paperId);

    @Delete("DELETE FROM exam_paper_question WHERE id = #{id}")
    int delete(Long id);
}
