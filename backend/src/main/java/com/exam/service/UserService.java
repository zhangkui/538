package com.exam.service;

import com.exam.entity.User;
import java.util.List;

public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAllStudents();
    List<User> findAllTeachers();
    User createUser(User user);
    int batchCreateStudents(List<User> students);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void deleteUser(Long id);
}
