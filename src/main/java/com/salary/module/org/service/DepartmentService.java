package com.salary.module.org.service;

import com.salary.module.org.domain.Department;
import com.salary.module.org.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public List<Department> getAll() { return departmentRepository.findByParentIsNull(); }
    public List<Department> getActive() { return departmentRepository.findByActiveTrue(); }
    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在: " + id));
    }
    @Transactional
    public Department create(Department department) { return departmentRepository.save(department); }
    @Transactional
    public Department update(Long id, Department dto) {
        Department dept = getById(id);
        dept.setName(dto.getName()); dept.setCode(dto.getCode()); dept.setSortOrder(dto.getSortOrder());
        if (dto.getParent() != null && !dto.getParent().getId().equals(id))
            dept.setParent(departmentRepository.findById(dto.getParent().getId()).orElse(null));
        return departmentRepository.save(dept);
    }
    @Transactional
    public void delete(Long id) { Department dept = getById(id); dept.setActive(false); departmentRepository.save(dept); }
}
