package com.salary.module.salary.item;

import com.salary.module.salary.item.domain.SalaryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalaryItemRepository extends JpaRepository<SalaryItem, Long> {
    List<SalaryItem> findByItemTypeOrderBySortOrder(String itemType);
    List<SalaryItem> findByActiveTrueOrderBySortOrder();
}
