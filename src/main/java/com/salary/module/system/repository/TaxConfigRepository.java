package com.salary.module.system.repository;

import com.salary.module.system.domain.TaxConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaxConfigRepository extends JpaRepository<TaxConfig, Long> {
    List<TaxConfig> findByYearAndActiveTrueOrderByLevel(Integer year);
}
