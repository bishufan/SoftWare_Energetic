package com.salary.module.salary.item;

import com.salary.web.ApiResponse;
import com.salary.module.salary.item.domain.SalaryItem;
import com.salary.module.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/salary-items")
@RequiredArgsConstructor
public class SalaryItemController {
    private final SalaryItemService salaryItemService;
    private final OperationLogService logService;

    @GetMapping
    public ApiResponse<List<SalaryItem>> getAll() { return ApiResponse.ok(salaryItemService.getAll()); }
    @GetMapping("/type/{itemType}")
    public ApiResponse<List<SalaryItem>> getByType(@PathVariable String itemType) { return ApiResponse.ok(salaryItemService.getByType(itemType)); }
    @GetMapping("/{id}")
    public ApiResponse<SalaryItem> getById(@PathVariable Long id) { return ApiResponse.ok(salaryItemService.getById(id)); }
    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<SalaryItem> create(@RequestBody SalaryItem item, HttpServletRequest req) {
        SalaryItem r = salaryItemService.create(item);
        logService.log(getUsername(req), "新增工资项", "项目名称: "+item.getName(), req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<SalaryItem> update(@PathVariable Long id, @RequestBody SalaryItem item, HttpServletRequest req) {
        SalaryItem r = salaryItemService.update(id, item);
        logService.log(getUsername(req), "修改工资项", "项目ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok(r);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest req) {
        salaryItemService.delete(id);
        logService.log(getUsername(req), "删除工资项", "项目ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok("删除成功", null);
    }
    private String getUsername(HttpServletRequest req) {
        return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "unknown";
    }
}
