package com.salary.module.auth;

import com.salary.web.ApiResponse;
import com.salary.web.LoginRequest;
import com.salary.module.system.domain.User;
import com.salary.module.system.repository.UserRepository;
import com.salary.common.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))
            return ApiResponse.fail("用户名或密码错误");
        if (!user.getActive()) return ApiResponse.fail("账号已禁用");
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ApiResponse.ok(Map.of("token", token, "username", user.getUsername(),
                "realName", user.getRealName(), "role", user.getRole()));
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody LoginRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) return ApiResponse.fail("用户名已存在");
        userRepository.save(User.builder().username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())).role("EMPLOYEE").active(true).build());
        return ApiResponse.ok("注册成功", null);
    }
}
