package com.salary.module.report;

import com.salary.web.ApiResponse;
import com.salary.module.report.dto.DeptSalarySummary;
import com.salary.module.report.dto.YearlyComparisonDto;
import com.salary.module.salary.calculation.service.ExcelService;
import com.salary.module.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ExcelService excelService;
    private final OperationLogService logService;

    @GetMapping("/department-summary")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','HR')")
    public ApiResponse<List<DeptSalarySummary>> departmentSummary(@RequestParam String yearMonth) {
        return ApiResponse.ok(reportService.getDepartmentSummary(yearMonth));
    }

    @GetMapping("/tax-report")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<List<Map<String, Object>>> taxReport(@RequestParam String yearMonth) {
        return ApiResponse.ok(reportService.getTaxReport(yearMonth));
    }

    @GetMapping("/tax-report/export")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ResponseEntity<byte[]> exportTaxReport(@RequestParam String yearMonth, HttpServletRequest req) {
        try {
            List<Map<String, Object>> data = reportService.getTaxReport(yearMonth);
            byte[] excelData = excelService.exportTaxReportToExcel(data);
            logService.log(getUsername(req), "导出个税申报表", "年月: "+yearMonth, req.getRemoteAddr(), true);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tax_report_"+yearMonth+".xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(excelData);
        } catch (Exception e) { return ResponseEntity.internalServerError().build(); }
    }

    @GetMapping("/yearly-comparison")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<List<YearlyComparisonDto>> yearlyComparison(@RequestParam String year) {
        return ApiResponse.ok(reportService.getYearlyComparison(year));
    }

    private String getUsername(HttpServletRequest req) { return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "unknown"; }
}
