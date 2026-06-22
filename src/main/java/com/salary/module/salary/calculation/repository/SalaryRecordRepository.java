package com.salary.module.salary.calculation.repository;

import com.salary.module.org.domain.Employee;
import com.salary.module.salary.calculation.domain.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {
    Optional<SalaryRecord> findByEmployeeIdAndYearMonth(Long employeeId, String yearMonth);
    List<SalaryRecord> findByEmployeeIdOrderByYearMonthDesc(Long employeeId);
    List<SalaryRecord> findByYearMonth(String yearMonth);
    List<SalaryRecord> findByYearMonthAndStatus(String yearMonth, String status);
    @Query("SELECT r FROM SalaryRecord r WHERE r.yearMonth = :ym AND r.employee.department.id = :deptId")
    List<SalaryRecord> findByYearMonthAndDepartment(@Param("ym") String ym, @Param("deptId") Long deptId);
    @Query("SELECT DISTINCT r.yearMonth FROM SalaryRecord r ORDER BY r.yearMonth DESC")
    List<String> findDistinctYearMonths();
    @Query("SELECT r FROM SalaryRecord r WHERE r.employee = :e AND r.yearMonth LIKE :prefix%")
    List<SalaryRecord> findByEmployeeAndYearPrefix(@Param("e") Employee e, @Param("prefix") String prefix);
}
