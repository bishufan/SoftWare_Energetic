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

    /** 获取所有部门（含子部门），用于前端表格显示 */
    @Transactional(readOnly = true)
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    /** 获取所有部门扁平列表（不含树形结构），用于下拉选择 */
    @Transactional(readOnly = true)
    public List<Department> getAllFlat() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Department> getActive() {
        return departmentRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在: " + id));
    }

    @Transactional
    public Department create(Department department) {
        // 校验必填字段
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
        if (department.getName().length() > 100) {
            throw new IllegalArgumentException("部门名称不能超过100个字符");
        }
        // 校验上级部门是否存在
        if (department.getParent() != null && department.getParent().getId() != null) {
            department.setParent(departmentRepository.findById(department.getParent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("上级部门不存在，ID: " + department.getParent().getId())));
        } else {
            department.setParent(null);
        }
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, Department dto) {
        Department dept = getById(id);
        if (dto.getName() != null) {
            if (dto.getName().trim().isEmpty()) throw new IllegalArgumentException("部门名称不能为空");
            if (dto.getName().length() > 100) throw new IllegalArgumentException("部门名称不能超过100个字符");
            dept.setName(dto.getName());
        }
        dept.setCode(dto.getCode());
        dept.setSortOrder(dto.getSortOrder());
        if (dto.getParent() != null && dto.getParent().getId() != null) {
            if (dto.getParent().getId().equals(id)) throw new IllegalArgumentException("上级部门不能是自己");
            dept.setParent(departmentRepository.findById(dto.getParent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("上级部门不存在，ID: " + dto.getParent().getId())));
        } else {
            dept.setParent(null);
        }
        return departmentRepository.save(dept);
    }

    @Transactional
    public void delete(Long id) {
        Department dept = getById(id);
        // 检查是否有子部门
        List<Department> children = departmentRepository.findByParentId(id);
        if (children != null && !children.isEmpty()) {
            throw new IllegalArgumentException("该部门下存在子部门，请先删除子部门");
        }
        dept.setActive(false);
        departmentRepository.save(dept);
    }
}
