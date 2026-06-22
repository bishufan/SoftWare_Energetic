package com.salary.module.salary.calculation.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SalaryCalculationRequest {
    private String yearMonth;
    private boolean trialRun;
    private Map<Long, Double> variableValues;
}
