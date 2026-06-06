package com.exam.mapper;

import com.exam.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);
    
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM user WHERE role = #{role}")
    List<User> findByRole(String role);
    
    @Select("SELECT * FROM user")
    List<User> findAll();
    
    @Insert("INSERT INTO user (username, password, real_name, role, need_change_pwd, class_name) " +
            "VALUES (#{username}, #{password}, #{realName}, #{role}, #{needChangePwd}, #{className})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE user SET password = #{password}, need_change_pwd = #{needChangePwd}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updatePassword(User user);
    
    @Update("UPDATE user SET real_name = #{realName}, class_name = #{className}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(User user);
    
    @Delete("DELETE FROM user WHERE id = #{id}")
    int delete(Long id);
    
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int countByUsername(String username);
    
    int batchInsert(List<User> users);
}
