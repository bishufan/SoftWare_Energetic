package com.salary.module.org.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "sys_employee")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String employeeNo;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(length = 20)
    private String idCard;
    @Column(length = 20)
    private String phone;
    @Column(length = 100)
    private String email;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    @Column(length = 20)
    private String position;
    @Column(nullable = false)
    private LocalDate hireDate;
    private LocalDate resignDate;
    @Column(nullable = false)
    private Boolean active = true;
    private Double socialSecurityBase;
    private Double housingFundBase;
    @Column(length = 100)
    private String socialSecurityCity;
    @Column(updatable = false)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @PrePersist
    protected void onCreate() { createTime = LocalDateTime.now(); updateTime = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updateTime = LocalDateTime.now(); }
}
