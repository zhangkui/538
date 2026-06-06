package com.exam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private Boolean needChangePwd;
    private String className;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
