package com.salary.module.report;

import com.salary.module.org.domain.Department;
import com.salary.module.org.repository.DepartmentRepository;
import com.salary.module.salary.calculation.domain.SalaryRecord;
import com.salary.module.salary.calculation.repository.SalaryRecordRepository;
import com.salary.module.report.dto.DeptSalarySummary;
import com.salary.module.report.dto.YearlyComparisonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final SalaryRecordRepository recordRepository;
    private final DepartmentRepository departmentRepository;

    public List<DeptSalarySummary> getDepartmentSummary(String yearMonth) {
        List<Department> departments = departmentRepository.findByActiveTrue();
        List<SalaryRecord> records = recordRepository.findByYearMonth(yearMonth);
        Map<Long, List<SalaryRecord>> deptMap = records.stream()
                .collect(Collectors.groupingBy(r -> r.getEmployee().getDepartment().getId()));
        return departments.stream().map(dept -> {
            List<SalaryRecord> list = deptMap.getOrDefault(dept.getId(), List.of());
            return new DeptSalarySummary(dept.getId(), dept.getName(), list.size(),
                    sum(list, SalaryRecord::getGrossPay), sum(list, SalaryRecord::getIncomeTax),
                    sum(list, SalaryRecord::getSocialInsurance), sum(list, SalaryRecord::getHousingFund),
                    sum(list, SalaryRecord::getNetPay));
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTaxReport(String yearMonth) {
        return recordRepository.findByYearMonth(yearMonth).stream().map(r -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("姓名", r.getEmployee().getName());
            row.put("身份证号", r.getEmployee().getIdCard());
            row.put("工资号", r.getEmployee().getEmployeeNo());
            row.put("累计收入", r.getCumulativeIncome());
            row.put("累计专项扣除", r.getCumulativeDeduction());
            row.put("累计已缴税额", r.getCumulativeTaxPaid());
            row.put("本月应纳税额", r.getIncomeTax());
            row.put("实发工资", r.getNetPay());
            return row;
        }).collect(Collectors.toList());
    }

    public List<YearlyComparisonDto> getYearlyComparison(String year) {
        List<Department> departments = departmentRepository.findByActiveTrue();
        List<YearlyComparisonDto> result = new ArrayList<>();
        for (Department dept : departments) {
            for (int m = 1; m <= 12; m++) {
                String ym = year + String.format("%02d", m);
                List<SalaryRecord> records = recordRepository.findByYearMonthAndDepartment(ym, dept.getId());
                if (!records.isEmpty()) {
                    result.add(new YearlyComparisonDto(dept.getName(), ym,
                            sum(records, SalaryRecord::getGrossPay), sum(records, SalaryRecord::getNetPay)));
                }
            }
        }
        return result;
    }

    private double sum(List<SalaryRecord> list, ToDoubleFunction<SalaryRecord> mapper) {
        return Math.round(list.stream().mapToDouble(mapper).sum() * 100.0) / 100.0;
    }
}
