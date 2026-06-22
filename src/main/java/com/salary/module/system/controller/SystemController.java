package com.salary.module.system.controller;

import com.salary.web.ApiResponse;
import com.salary.module.system.domain.OperationLog;
import com.salary.module.system.domain.SocialSecurityConfig;
import com.salary.module.system.domain.User;
import com.salary.module.system.repository.SocialSecurityConfigRepository;
import com.salary.module.system.repository.TaxConfigRepository;
import com.salary.module.system.repository.UserRepository;
import com.salary.module.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SocialSecurityConfigRepository socialSecurityRepository;
    private final TaxConfigRepository taxConfigRepository;
    private final OperationLogService logService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<User>> getUsers() { return ApiResponse.ok(userRepository.findAll()); }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> createUser(@RequestBody User user, HttpServletRequest req) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        logService.log(getUsername(req), "新增用户", "用户名: "+user.getUsername(), req.getRemoteAddr(), true);
        return ApiResponse.ok(saved);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User dto, HttpServletRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setRealName(dto.getRealName()); user.setEmail(dto.getEmail()); user.setRole(dto.getRole()); user.setActive(dto.getActive());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) user.setPassword(passwordEncoder.encode(dto.getPassword()));
        User saved = userRepository.save(user);
        logService.log(getUsername(req), "修改用户", "用户ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteUser(@PathVariable Long id, HttpServletRequest req) {
        userRepository.deleteById(id);
        logService.log(getUsername(req), "删除用户", "用户ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok("删除成功", null);
    }

    @GetMapping("/social-security")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<List<SocialSecurityConfig>> getSocialSecurity(
            @RequestParam(required=false) String city, @RequestParam(required=false) Integer year) {
        if (city != null && year != null) return ApiResponse.ok(socialSecurityRepository.findByCityAndYearAndActiveTrue(city, year));
        if (year != null) return ApiResponse.ok(socialSecurityRepository.findByYearAndActiveTrue(year));
        if (city != null) return ApiResponse.ok(socialSecurityRepository.findByCityAndActiveTrue(city));
        return ApiResponse.ok(socialSecurityRepository.findAll());
    }

    @PostMapping("/social-security")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<SocialSecurityConfig> createSocialSecurity(@RequestBody SocialSecurityConfig config, HttpServletRequest req) {
        SocialSecurityConfig r = socialSecurityRepository.save(config);
        logService.log(getUsername(req), "新增社保配置", config.getCity()+"-"+config.getInsuranceType(), req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }

    @PutMapping("/social-security/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<SocialSecurityConfig> updateSocialSecurity(@PathVariable Long id, @RequestBody SocialSecurityConfig dto, HttpServletRequest req) {
        SocialSecurityConfig c = socialSecurityRepository.findById(id).orElseThrow(() -> new RuntimeException("社保配置不存在"));
        c.setPersonalRate(dto.getPersonalRate()); c.setCompanyRate(dto.getCompanyRate());
        c.setBaseLower(dto.getBaseLower()); c.setBaseUpper(dto.getBaseUpper()); c.setActive(dto.getActive());
        SocialSecurityConfig r = socialSecurityRepository.save(c);
        logService.log(getUsername(req), "修改社保配置", "配置ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OperationLog>> getLogs(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ApiResponse.ok(logService.getLogs(PageRequest.of(page, size)));
    }

    private String getUsername(HttpServletRequest req) { return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "unknown"; }
}
