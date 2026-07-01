package com.salary.module.org.controller;

import com.salary.web.ApiResponse;
import com.salary.module.org.domain.Department;
import com.salary.module.org.service.DepartmentService;
import com.salary.module.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final OperationLogService logService;

    @GetMapping
    public ApiResponse<List<Department>> getAll() { return ApiResponse.ok(departmentService.getAll()); }
    @GetMapping("/active")
    public ApiResponse<List<Department>> getActive() { return ApiResponse.ok(departmentService.getActive()); }
    @GetMapping("/all")
    public ApiResponse<List<Department>> getAllFlat() { return ApiResponse.ok(departmentService.getAllFlat()); }
    @GetMapping("/{id}")
    public ApiResponse<Department> getById(@PathVariable Long id) { return ApiResponse.ok(departmentService.getById(id)); }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<Department> create(@RequestBody Department d, HttpServletRequest req) {
        Department r = departmentService.create(d);
        logService.log(getUsername(req), "新增部门", "部门名称: "+d.getName(), req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<Department> update(@PathVariable Long id, @RequestBody Department d, HttpServletRequest req) {
        Department r = departmentService.update(id, d);
        logService.log(getUsername(req), "修改部门", "部门ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest req) {
        departmentService.delete(id);
        logService.log(getUsername(req), "删除部门", "部门ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok("删除成功", null);
    }
    private String getUsername(HttpServletRequest req) {
        return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "unknown";
    }
}
