package com.exam.controller;

import com.exam.dto.*;
import com.exam.entity.User;
import com.exam.service.UserService;
import com.exam.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户登录、修改密码等")
public class AuthController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户登录成功: {}", user.getUsername());
        
        return ApiResponse.success(new LoginResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getRole(),
            user.getNeedChangePwd()
        ));
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "修改密码")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("密码修改成功", null);
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<User> getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findById(userId);
        user.setPassword(null);
        return ApiResponse.success(user);
    }
}
