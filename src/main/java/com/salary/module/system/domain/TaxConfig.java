package com.salary.module.system.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "sys_tax_config")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer level;
    private Double minIncome;
    private Double maxIncome;
    @Column(nullable = false)
    private Double taxRate;
    private Double quickDeduction;
    @Column(nullable = false)
    private Integer year;
    @Column(nullable = false)
    private Boolean active = true;
}
