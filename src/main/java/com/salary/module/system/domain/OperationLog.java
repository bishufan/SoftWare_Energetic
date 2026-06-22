package com.salary.module.system.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "sys_operation_log")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OperationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String username;
    @Column(nullable = false, length = 50)
    private String operation;
    @Column(length = 200)
    private String detail;
    @Column(length = 50)
    private String ipAddress;
    @Column(nullable = false, length = 20)
    private String result; // SUCCESS/FAILED
    @Column(updatable = false)
    private LocalDateTime createTime;
    @PrePersist
    protected void onCreate() { createTime = LocalDateTime.now(); }
}
