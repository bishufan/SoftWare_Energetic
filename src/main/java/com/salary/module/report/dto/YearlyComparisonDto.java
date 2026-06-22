package com.salary.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class YearlyComparisonDto {
    private String departmentName;
    private String month;
    private Double grossPay;
    private Double netPay;
}
