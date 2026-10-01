package com.njung.moneyflow.category.repository;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndTypeAndParentCategoryIsNull(String name, TransactionType type);

    boolean existsByNameAndTypeAndParentCategoryId(String name, TransactionType type, Long parentId);

    List<Category> findAllByDeletedAtIsNull();

    List<Category> findAllByActiveTrueAndDeletedAtIsNull();
}
