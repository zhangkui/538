package com.exam.mapper;

import com.exam.entity.Question;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionMapper {
    
    @Select("SELECT q.*, u.real_name as creator_name FROM question q " +
            "LEFT JOIN user u ON q.created_by = u.id WHERE q.id = #{id}")
    Question findById(Long id);
    
    @Select("SELECT q.*, u.real_name as creator_name FROM question q " +
            "LEFT JOIN user u ON q.created_by = u.id ORDER BY q.created_at DESC")
    List<Question> findAll();
    
    @Select("SELECT q.*, u.real_name as creator_name FROM question q " +
            "LEFT JOIN user u ON q.created_by = u.id " +
            "WHERE q.created_by = #{createdBy} ORDER BY q.created_at DESC")
    List<Question> findByCreator(Long createdBy);
    
    @Insert("INSERT INTO question (title, description, requirements, sample_input, sample_output, " +
            "reference_answer, score, difficulty, created_by) VALUES (#{title}, #{description}, " +
            "#{requirements}, #{sampleInput}, #{sampleOutput}, #{referenceAnswer}, #{score}, " +
            "#{difficulty}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Question question);
    
    @Update("UPDATE question SET title = #{title}, description = #{description}, " +
            "requirements = #{requirements}, sample_input = #{sampleInput}, " +
            "sample_output = #{sampleOutput}, reference_answer = #{referenceAnswer}, " +
            "score = #{score}, difficulty = #{difficulty}, updated_at = NOW() WHERE id = #{id}")
    int update(Question question);
    
    @Delete("DELETE FROM question WHERE id = #{id}")
    int delete(Long id);
    
    int batchInsert(List<Question> questions);
}
