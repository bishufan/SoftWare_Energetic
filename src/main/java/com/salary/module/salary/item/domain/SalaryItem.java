package com.salary.module.salary.item.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "sys_salary_item")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SalaryItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 50)
    private String code;
    @Column(nullable = false, length = 30)
    private String itemType; // EARNING/DEDUCTION/TAX/SOCIAL/FUND/NET
    @Column(length = 500)
    private String formula;
    @Column(nullable = false)
    private Boolean builtIn = false;
    private Integer sortOrder;
    @Column(nullable = false)
    private Boolean active = true;
}
