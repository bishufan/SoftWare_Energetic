package com.salary.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class DeptSalarySummary {
    private Long departmentId;
    private String departmentName;
    private int employeeCount;
    private Double grossPay;
    private Double totalTax;
    private Double totalSocial;
    private Double totalFund;
    private Double netPay;
}
