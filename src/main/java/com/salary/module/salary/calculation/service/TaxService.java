package com.salary.module.salary.calculation.service;

import com.salary.module.system.domain.TaxConfig;
import com.salary.module.system.repository.TaxConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxService {
    private final TaxConfigRepository taxConfigRepository;

    public double calculateTax(String yearMonth, double monthlyIncome, double cumulativeIncome,
                                double cumulativeExempt, double cumulativeDeduction,
                                double cumulativeTaxPaid, double specialDeduction) {
        int year = Integer.parseInt(yearMonth.substring(0, 4));
        int month = Integer.parseInt(yearMonth.substring(4));
        double cumulativeThreshold = 5000.0 * month;
        double taxableIncome = cumulativeIncome - cumulativeExempt - cumulativeThreshold
                - cumulativeDeduction - specialDeduction;
        if (taxableIncome <= 0) return 0;

        List<TaxConfig> brackets = taxConfigRepository.findByYearAndActiveTrueOrderByLevel(year);
        if (brackets.isEmpty()) brackets = getDefaultBrackets();

        double cumulativeTax = 0, remaining = taxableIncome;
        for (TaxConfig b : brackets) {
            if (remaining <= 0) break;
            double max = b.getMaxIncome() == null ? remaining : b.getMaxIncome();
            double amount = Math.min(remaining, max - b.getMinIncome());
            cumulativeTax += amount * b.getTaxRate() / 100.0;
            remaining -= amount;
        }
        return Math.max(0, Math.round((cumulativeTax - cumulativeTaxPaid) * 100.0) / 100.0);
    }

    private List<TaxConfig> getDefaultBrackets() {
        return List.of(
            TaxConfig.builder().level(1).minIncome(0.0).maxIncome(36000.0).taxRate(3.0).build(),
            TaxConfig.builder().level(2).minIncome(36000.0).maxIncome(144000.0).taxRate(10.0).quickDeduction(2520.0).build(),
            TaxConfig.builder().level(3).minIncome(144000.0).maxIncome(300000.0).taxRate(20.0).quickDeduction(16920.0).build(),
            TaxConfig.builder().level(4).minIncome(300000.0).maxIncome(420000.0).taxRate(25.0).quickDeduction(31920.0).build(),
            TaxConfig.builder().level(5).minIncome(420000.0).maxIncome(660000.0).taxRate(30.0).quickDeduction(52920.0).build(),
            TaxConfig.builder().level(6).minIncome(660000.0).maxIncome(960000.0).taxRate(35.0).quickDeduction(85920.0).build(),
            TaxConfig.builder().level(7).minIncome(960000.0).maxIncome(null).taxRate(45.0).quickDeduction(181920.0).build()
        );
    }
}
