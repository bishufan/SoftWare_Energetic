package com.salary.module.system.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_social_security_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialSecurityConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "insurance_type", nullable = false, length = 30)
    private String insuranceType;

    @Column(name = "personal_rate", nullable = false)
    private Double personalRate;

    @Column(name = "company_rate", nullable = false)
    private Double companyRate;

    @Column(name = "base_lower")
    private Double baseLower;

    @Column(name = "base_upper")
    private Double baseUpper;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "year_val", nullable = false)
    private Integer year;
}
