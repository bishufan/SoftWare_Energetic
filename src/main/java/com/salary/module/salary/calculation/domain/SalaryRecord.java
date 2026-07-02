package com.salary.module.salary.calculation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.salary.module.org.domain.Employee;
import com.salary.module.system.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "sal_salary_record")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"employee", "processedBy"})
public class SalaryRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;
    @Column(nullable = false, length = 6)
    private String yearMonth;
    @Column(columnDefinition = "TEXT")
    private String itemValues;
    private Double grossPay;
    private Double incomeTax;
    private Double socialInsurance;
    private Double housingFund;
    private Double netPay;
    private Double cumulativeIncome;
    private Double cumulativeExempt;
    private Double cumulativeDeduction;
    private Double cumulativeTaxPaid;
    @Column(nullable = false, length = 20)
    private String status; // DRAFT/CONFIRMED/PAID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User processedBy;
    private LocalDateTime processTime;
    @Column(updatable = false)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @PrePersist
    protected void onCreate() { createTime = LocalDateTime.now(); updateTime = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updateTime = LocalDateTime.now(); }
}
