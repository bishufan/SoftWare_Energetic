package com.salary.module.salary.calculation.controller;

import com.salary.web.ApiResponse;
import com.salary.module.salary.calculation.dto.SalaryCalculationRequest;
import com.salary.module.salary.calculation.domain.SalaryRecord;
import com.salary.module.salary.calculation.service.SalaryCalculationService;
import com.salary.module.salary.calculation.service.ExcelService;
import com.salary.module.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {
    private final SalaryCalculationService calculationService;
    private final ExcelService excelService;
    private final OperationLogService logService;

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<List<SalaryRecord>> calculate(@RequestBody SalaryCalculationRequest req, HttpServletRequest httpReq) {
        String username = httpReq.getUserPrincipal().getName();
        List<SalaryRecord> records = calculationService.calculateAll(req.getYearMonth(), req.isTrialRun(), username);
        logService.log(username, "工资计算", "年月: "+req.getYearMonth()+", 试算: "+req.isTrialRun(), httpReq.getRemoteAddr(), true);
        return ApiResponse.ok(records);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<List<SalaryRecord>> getRecords(@RequestParam String yearMonth, @RequestParam(required=false) Long departmentId) {
        return ApiResponse.ok(calculationService.getRecords(yearMonth, departmentId));
    }

    @GetMapping("/records/employee/{employeeId}")
    public ApiResponse<List<SalaryRecord>> getEmployeeRecords(@PathVariable Long employeeId) {
        return ApiResponse.ok(calculationService.getEmployeeRecords(employeeId));
    }

    @GetMapping("/records/employee/{employeeId}/{yearMonth}")
    public ApiResponse<SalaryRecord> getEmployeeMonthRecord(@PathVariable Long employeeId, @PathVariable String yearMonth) {
        return ApiResponse.ok(calculationService.getEmployeeMonthRecord(employeeId, yearMonth));
    }

    @PostMapping("/records/{id}/confirm")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<?> confirm(@PathVariable Long id, HttpServletRequest req) {
        logService.log(getUsername(req), "确认工资记录", "记录ID: "+id, req.getRemoteAddr(), true);
        return ApiResponse.ok("确认成功", null);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<?> importData(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        try {
            Map<String, Map<Long, Double>> data = excelService.importVariableData(file);
            logService.log(getUsername(req), "导入工资数据", "导入员工数: "+data.size(), req.getRemoteAddr(), true);
            return ApiResponse.ok("导入成功", data);
        } catch (Exception e) { return ApiResponse.fail("导入失败: "+e.getMessage()); }
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ResponseEntity<byte[]> export(@RequestParam String yearMonth, @RequestParam(required=false) Long departmentId, HttpServletRequest req) {
        try {
            List<SalaryRecord> records = calculationService.getRecords(yearMonth, departmentId);
            byte[] data = excelService.exportToExcel(records);
            logService.log(getUsername(req), "导出工资表", "年月: "+yearMonth, req.getRemoteAddr(), true);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=salary_"+yearMonth+".xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(data);
        } catch (Exception e) { return ResponseEntity.internalServerError().build(); }
    }

    private String getUsername(HttpServletRequest req) { return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "unknown"; }
}
