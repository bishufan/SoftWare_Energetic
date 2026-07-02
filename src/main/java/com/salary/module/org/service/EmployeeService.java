package com.salary.module.org.service;

import com.salary.module.org.domain.Employee;
import com.salary.module.org.repository.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<Employee> search(Map<String,String> params, Pageable pageable) {
        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(params.get("name")))
                predicates.add(cb.like(root.get("name"), "%"+params.get("name")+"%"));
            if (StringUtils.hasText(params.get("employeeNo")))
                predicates.add(cb.like(root.get("employeeNo"), "%"+params.get("employeeNo")+"%"));
            if (StringUtils.hasText(params.get("departmentId")))
                predicates.add(cb.equal(root.get("department").get("id"), Long.valueOf(params.get("departmentId"))));
            if (StringUtils.hasText(params.get("active")))
                predicates.add(cb.equal(root.get("active"), Boolean.valueOf(params.get("active"))));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return employeeRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("员工不存在: " + id));
    }
    @Transactional(readOnly = true)
    public List<Employee> getActiveEmployees() { return employeeRepository.findByActiveTrue(); }

    @Transactional
    public Employee create(Employee employee) { return employeeRepository.save(employee); }

    @Transactional
    public Employee update(Long id, Employee dto) {
        Employee emp = getById(id);
        emp.setName(dto.getName()); emp.setIdCard(dto.getIdCard()); emp.setPhone(dto.getPhone());
        emp.setEmail(dto.getEmail()); emp.setDepartment(dto.getDepartment()); emp.setPosition(dto.getPosition());
        emp.setHireDate(dto.getHireDate()); emp.setResignDate(dto.getResignDate());
        emp.setSocialSecurityBase(dto.getSocialSecurityBase()); emp.setHousingFundBase(dto.getHousingFundBase());
        emp.setSocialSecurityCity(dto.getSocialSecurityCity());
        return employeeRepository.save(emp);
    }

    @Transactional
    public void delete(Long id) { Employee emp = getById(id); emp.setActive(false); employeeRepository.save(emp); }
}
