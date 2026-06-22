package com.salary.module.system.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_tax_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tax_level", nullable = false)
    private Integer level;

    @Column(name = "min_income")
    private Double minIncome;

    @Column(name = "max_income")
    private Double maxIncome;

    @Column(name = "tax_rate", nullable = false)
    private Double taxRate;

    @Column(name = "quick_deduction")
    private Double quickDeduction;

    @Column(name = "tax_year", nullable = false)
    private Integer year;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
