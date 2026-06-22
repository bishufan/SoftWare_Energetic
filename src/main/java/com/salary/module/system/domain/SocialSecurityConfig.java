package com.salary.module.system.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "sys_social_security_config")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SocialSecurityConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100)
    private String city;
    @Column(nullable = false, length = 30)
    private String insuranceType; // ENDOWMENT/MEDICAL/UNEMPLOYMENT/INJURY/MATERNITY/HOUSING_FUND
    @Column(nullable = false)
    private Double personalRate;
    @Column(nullable = false)
    private Double companyRate;
    private Double baseLower;
    private Double baseUpper;
    @Column(nullable = false)
    private Boolean active = true;
    @Column(nullable = false)
    private Integer year;
}
