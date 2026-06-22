package com.salary.module.salary.calculation.service;

import com.salary.module.org.domain.Employee;
import com.salary.module.system.domain.SocialSecurityConfig;
import com.salary.module.system.repository.SocialSecurityConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialSecurityService {
    private final SocialSecurityConfigRepository configRepository;

    public Map<String, Double> calculate(Employee employee, String yearMonth) {
        int year = Integer.parseInt(yearMonth.substring(0, 4));
        String city = employee.getSocialSecurityCity();
        if (city == null || city.isBlank()) city = "default";
        List<SocialSecurityConfig> configs = configRepository.findByCityAndYearAndActiveTrue(city, year);
        if (configs.isEmpty()) configs = configRepository.findByYearAndActiveTrue(year);
        if (configs.isEmpty()) configs = getDefaultConfigs();

        double socialInsurance = 0, housingFund = 0;
        for (SocialSecurityConfig cfg : configs) {
            double base = "HOUSING_FUND".equals(cfg.getInsuranceType()) ?
                    (employee.getHousingFundBase() != null ? employee.getHousingFundBase() : 0) :
                    (employee.getSocialSecurityBase() != null ? employee.getSocialSecurityBase() : 0);
            if (cfg.getBaseLower() != null && base < cfg.getBaseLower()) base = cfg.getBaseLower();
            if (cfg.getBaseUpper() != null && base > cfg.getBaseUpper()) base = cfg.getBaseUpper();
            if ("HOUSING_FUND".equals(cfg.getInsuranceType()))
                housingFund += base * cfg.getPersonalRate() / 100.0;
            else
                socialInsurance += base * cfg.getPersonalRate() / 100.0;
        }
        Map<String, Double> result = new HashMap<>();
        result.put("socialInsurance", Math.round(socialInsurance * 100.0) / 100.0);
        result.put("housingFund", Math.round(housingFund * 100.0) / 100.0);
        return result;
    }

    private List<SocialSecurityConfig> getDefaultConfigs() {
        return List.of(
            SocialSecurityConfig.builder().city("default").insuranceType("ENDOWMENT").personalRate(8.0).companyRate(16.0).build(),
            SocialSecurityConfig.builder().city("default").insuranceType("MEDICAL").personalRate(2.0).companyRate(8.0).build(),
            SocialSecurityConfig.builder().city("default").insuranceType("UNEMPLOYMENT").personalRate(0.5).companyRate(0.5).build(),
            SocialSecurityConfig.builder().city("default").insuranceType("INJURY").personalRate(0.0).companyRate(0.5).build(),
            SocialSecurityConfig.builder().city("default").insuranceType("MATERNITY").personalRate(0.0).companyRate(0.5).build(),
            SocialSecurityConfig.builder().city("default").insuranceType("HOUSING_FUND").personalRate(7.0).companyRate(7.0).build()
        );
    }
}
