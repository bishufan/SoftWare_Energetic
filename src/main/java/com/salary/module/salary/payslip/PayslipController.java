package com.salary.module.salary.payslip;

import com.salary.web.ApiResponse;
import com.salary.module.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payslips")
@RequiredArgsConstructor
public class PayslipController {
    private final PayslipService payslipService;
    private final OperationLogService logService;

    @GetMapping("/{recordId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long recordId, HttpServletRequest req) {
        byte[] data = payslipService.generatePdf(recordId);
        logService.log(getUsername(req), "下载工资条PDF", "记录ID: "+recordId, req.getRemoteAddr(), true);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip_"+recordId+".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(data);
    }

    @GetMapping("/{recordId}/html")
    public ApiResponse<String> viewHtml(@PathVariable Long recordId) {
        return ApiResponse.ok(payslipService.generateHtml(recordId));
    }

    @PostMapping("/{recordId}/push")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<?> pushPayslip(@PathVariable Long recordId, HttpServletRequest req) {
        logService.log(getUsername(req), "推送工资条", "记录ID: "+recordId, req.getRemoteAddr(), true);
        return ApiResponse.ok("推送成功", null);
    }

    private String getUsername(HttpServletRequest req) { return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "unknown"; }
}
