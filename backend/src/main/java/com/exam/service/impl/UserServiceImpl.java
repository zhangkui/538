package com.exam.service.impl;

import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public List<User> findAllStudents() {
        return userMapper.findByRole("STUDENT");
    }
    
    @Override
    public List<User> findAllTeachers() {
        return userMapper.findByRole("TEACHER");
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        if (userMapper.countByUsername(user.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        log.info("创建用户成功: {}", user.getUsername());
        return user;
    }
    
    @Override
    @Transactional
    public int batchCreateStudents(List<User> students) {
        for (User student : students) {
            student.setPassword(passwordEncoder.encode("123456"));
            student.setRole("STUDENT");
            student.setNeedChangePwd(true);
        }
        int count = userMapper.batchInsert(students);
        log.info("批量创建学生成功，数量: {}", count);
        return count;
    }
    
    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setNeedChangePwd(false);
        userMapper.updatePassword(user);
        log.info("用户修改密码成功: {}", user.getUsername());
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        userMapper.delete(id);
        log.info("删除用户成功: {}", id);
    }
}
