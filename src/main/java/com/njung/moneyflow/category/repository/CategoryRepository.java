package com.njung.moneyflow.category.repository;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndTypeAndParentCategoryIsNull(String name, TransactionType type);

    boolean existsByNameAndTypeAndParentCategoryId(String name, TransactionType type, Long parentId);

    boolean existsByParentCategory_IdAndDeletedAtIsNull(Long parentCategoryId);

    List<Category> findAllByDeletedAtIsNull();

    List<Category> findAllByActiveTrueAndDeletedAtIsNull();

    @Query("""
        SELECT COUNT(c) > 0
        FROM Category c
        LEFT JOIN c.parentCategory p
        WHERE c.name = :name
          AND c.type = :type
          AND c.deletedAt IS NULL
          AND (:categoryId IS NULL OR c.id <> :categoryId)
          AND (
              (:parentId IS NULL AND p IS NULL)
              OR p.id = :parentId
          )
        """)
    boolean existsDuplicateName(String name, TransactionType type, Long parentId, Long categoryId);
}
