package com.salary.module.org.repository;

import com.salary.module.org.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByParentIsNull();
    List<Department> findByActiveTrue();
    List<Department> findByParentId(Long parentId);
}
