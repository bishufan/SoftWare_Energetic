package com.salary.module.org.controller;

import com.salary.web.ApiResponse;
import com.salary.module.org.domain.Employee;
import com.salary.module.org.service.EmployeeService;
import com.salary.module.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final OperationLogService logService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<Page<Employee>> search(@RequestParam Map<String,String> params,
                                              @RequestParam(defaultValue="0") int page,
                                              @RequestParam(defaultValue="20") int size) {
        return ApiResponse.ok(employeeService.search(params, PageRequest.of(page, size)));
    }
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<?> getAllActive() { return ApiResponse.ok(employeeService.getActiveEmployees()); }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<Employee> getById(@PathVariable Long id) { return ApiResponse.ok(employeeService.getById(id)); }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<Employee> create(@RequestBody Employee e, HttpServletRequest req) {
        Employee r = employeeService.create(e);
        logService.log(getUsername(req), "新增员工", "员工: "+e.getName(), req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<Employee> update(@PathVariable Long id, @RequestBody Employee e, HttpServletRequest req) {
        Employee r = employeeService.update(id, e);
        logService.log(getUsername(req), "修改员工", "员工ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest req) {
        employeeService.delete(id);
        logService.log(getUsername(req), "删除员工", "员工ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok("删除成功", null);
    }
    private String getUsername(HttpServletRequest req) {
        return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "unknown";
    }
}
