package com.salary.module.salary.item;

import com.salary.module.salary.item.domain.SalaryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryItemService {
    private final SalaryItemRepository salaryItemRepository;

    public List<SalaryItem> getAll() { return salaryItemRepository.findByActiveTrueOrderBySortOrder(); }
    public List<SalaryItem> getByType(String itemType) { return salaryItemRepository.findByItemTypeOrderBySortOrder(itemType); }
    public SalaryItem getById(Long id) {
        return salaryItemRepository.findById(id).orElseThrow(() -> new RuntimeException("工资项不存在: " + id));
    }
    @Transactional
    public SalaryItem create(SalaryItem item) { return salaryItemRepository.save(item); }
    @Transactional
    public SalaryItem update(Long id, SalaryItem dto) {
        SalaryItem item = getById(id);
        item.setName(dto.getName()); item.setCode(dto.getCode()); item.setItemType(dto.getItemType());
        item.setFormula(dto.getFormula()); item.setSortOrder(dto.getSortOrder());
        return salaryItemRepository.save(item);
    }
    @Transactional
    public void delete(Long id) { SalaryItem item = getById(id); item.setActive(false); salaryItemRepository.save(item); }
}
