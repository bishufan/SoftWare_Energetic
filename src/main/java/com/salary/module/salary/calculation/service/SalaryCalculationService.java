package com.salary.module.salary.calculation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salary.module.org.domain.Employee;
import com.salary.module.org.repository.EmployeeRepository;
import com.salary.module.salary.calculation.domain.SalaryRecord;
import com.salary.module.salary.calculation.repository.SalaryRecordRepository;
import com.salary.module.salary.item.SalaryItemRepository;
import com.salary.module.salary.item.domain.SalaryItem;
import com.salary.module.system.domain.User;
import com.salary.module.system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryCalculationService {
    private final SalaryRecordRepository recordRepository;
    private final SalaryItemRepository salaryItemRepository;
    private final EmployeeRepository employeeRepository;
    private final TaxService taxService;
    private final SocialSecurityService socialSecurityService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<SalaryRecord> calculateAll(String yearMonth, boolean trialRun, String username) {
        List<Employee> employees = employeeRepository.findByActiveTrue();
        User operator = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<SalaryItem> items = salaryItemRepository.findByActiveTrueOrderBySortOrder();
        List<SalaryRecord> results = new ArrayList<>();
        for (Employee emp : employees) {
            SalaryRecord record = calculateSingle(emp, yearMonth, items, null);
            record.setStatus(trialRun ? "DRAFT" : "CONFIRMED");
            record.setProcessedBy(operator);
            record.setProcessTime(LocalDateTime.now());
            results.add(recordRepository.save(record));
        }
        log.info("工资计算完成: {} 员工, 年月: {}, 试算: {}", results.size(), yearMonth, trialRun);
        return results;
    }

    @Transactional
    public SalaryRecord calculateSingle(Employee employee, String yearMonth,
                                         List<SalaryItem> items, Map<Long, Double> variableValues) {
        String lastMonth = getLastMonth(yearMonth);
        Optional<SalaryRecord> lastRecord = recordRepository.findByEmployeeIdAndYearMonth(employee.getId(), lastMonth);
        Map<String, Double> itemValueMap = new LinkedHashMap<>();
        double grossPay = 0, socialInsurance = 0, housingFund = 0;

        for (SalaryItem item : items) {
            double value = evaluateItem(item, employee, yearMonth, variableValues, itemValueMap);
            itemValueMap.put(item.getCode(), value);
            switch (item.getItemType()) {
                case "EARNING" -> grossPay += value;
                case "SOCIAL" -> socialInsurance += Math.abs(value);
                case "FUND" -> housingFund += Math.abs(value);
            }
        }

        double cumulativeIncome = grossPay + (lastRecord.isPresent() ? lastRecord.get().getCumulativeIncome() : 0);
        double cumulativeExempt = lastRecord.isPresent() ? lastRecord.get().getCumulativeExempt() : 0;
        double cumulativeDeduction = (socialInsurance + housingFund) + (lastRecord.isPresent() ? lastRecord.get().getCumulativeDeduction() : 0);
        double cumulativeTaxPaid = lastRecord.isPresent() ? lastRecord.get().getCumulativeTaxPaid() : 0;
        double incomeTax = taxService.calculateTax(yearMonth, grossPay, cumulativeIncome, cumulativeExempt, cumulativeDeduction, cumulativeTaxPaid, 0);
        double netPay = grossPay - incomeTax - socialInsurance - housingFund;

        try {
            SalaryRecord record = SalaryRecord.builder()
                    .employee(employee).yearMonth(yearMonth)
                    .itemValues(objectMapper.writeValueAsString(itemValueMap))
                    .grossPay(round(grossPay)).incomeTax(incomeTax)
                    .socialInsurance(socialInsurance).housingFund(housingFund).netPay(round(netPay))
                    .cumulativeIncome(round(cumulativeIncome)).cumulativeExempt(cumulativeExempt)
                    .cumulativeDeduction(round(cumulativeDeduction))
                    .cumulativeTaxPaid(round(cumulativeTaxPaid + incomeTax))
                    .status("DRAFT").build();
            return recordRepository.save(record);
        } catch (Exception e) {
            throw new RuntimeException("工资计算序列化失败: " + e.getMessage());
        }
    }

    private double evaluateItem(SalaryItem item, Employee emp, String ym,
                                 Map<Long, Double> variableValues, Map<String, Double> calculated) {
        if ("TAX".equals(item.getItemType())) return 0;
        String formula = item.getFormula();
        if (formula == null || formula.isBlank()) {
            if (variableValues != null && variableValues.containsKey(item.getId()))
                return variableValues.get(item.getId());
            return switch (item.getCode()) {
                case "basePay" -> 5000.0; case "seniorityPay" -> 500.0; case "meritPay" -> 1000.0;
                default -> 0.0;
            };
        }
        String eval = formula;
        for (Map.Entry<String, Double> e : calculated.entrySet())
            eval = eval.replace(e.getKey(), String.valueOf(e.getValue()));
        try { return evaluateSimple(eval); }
        catch (Exception e) { log.warn("公式计算失败: {} = {}", item.getName(), formula); return 0; }
    }

    private double evaluateSimple(String expr) {
        expr = expr.replaceAll("\\s+", "");
        if (expr.isEmpty()) return 0;
        String[] parts = expr.split("(?=[+-])");
        double result = 0;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            char sign = '+'; String numStr = part;
            if (part.charAt(0) == '+' || part.charAt(0) == '-') { sign = part.charAt(0); numStr = part.substring(1); }
            result += (sign == '+' ? Double.parseDouble(numStr) : -Double.parseDouble(numStr));
        }
        return result;
    }

    private String getLastMonth(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4)), m = Integer.parseInt(ym.substring(4));
        return m == 1 ? (y - 1) + "12" : ym.substring(0, 4) + String.format("%02d", m - 1);
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }

    // --- Query Methods ---
    public List<SalaryRecord> getRecords(String yearMonth, Long departmentId) {
        return departmentId != null ? recordRepository.findByYearMonthAndDepartment(yearMonth, departmentId) : recordRepository.findByYearMonth(yearMonth);
    }
    public List<SalaryRecord> getEmployeeRecords(Long employeeId) { return recordRepository.findByEmployeeIdOrderByYearMonthDesc(employeeId); }
    public SalaryRecord getEmployeeMonthRecord(Long employeeId, String yearMonth) {
        return recordRepository.findByEmployeeIdAndYearMonth(employeeId, yearMonth)
                .orElseThrow(() -> new RuntimeException("工资记录不存在"));
    }
}
