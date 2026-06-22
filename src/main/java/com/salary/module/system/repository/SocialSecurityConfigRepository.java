package com.salary.module.system.repository;

import com.salary.module.system.domain.SocialSecurityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SocialSecurityConfigRepository extends JpaRepository<SocialSecurityConfig, Long> {
    List<SocialSecurityConfig> findByCityAndYearAndActiveTrue(String city, Integer year);
    List<SocialSecurityConfig> findByYearAndActiveTrue(Integer year);
    List<SocialSecurityConfig> findByCityAndActiveTrue(String city);
}
