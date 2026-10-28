package com.njung.moneyflow.category.service;

import com.njung.moneyflow.category.dto.CategoryRequest;
import com.njung.moneyflow.category.dto.CategoryUpdateRequest;
import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.entity.CategoryType;
import com.njung.moneyflow.category.repository.CategoryRepository;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
import com.njung.moneyflow.transaction.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Long create(CategoryRequest request) {
        Category parent = findParent(request.parentId());

        validateDuplicate(
            request.name(),
            request.type(),
            parent != null ? parent.getId() : null,
            null
        );

        Category category = new Category(
            request.name(),
            request.type(),
            CategoryType.CUSTOM,
            parent
        );

        return categoryRepository.save(category).getId();
    }

    @Transactional
    public void update(Long categoryId, CategoryUpdateRequest request) {
        Category category = getCategory(categoryId);

        if (Objects.equals(category.getName(), request.name())) {
            return;
        }

        Category parent = category.getParentCategory();
        Long parentId = parent != null ? parent.getId() : null;

        validateDuplicate(request.name(), category.getType(), parentId, categoryId);

        category.rename(request.name());
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = getCategory(categoryId);

        category.validateDeletable();

        boolean hasChildren = categoryRepository.existsByParentCategory_IdAndDeletedAtIsNull(
            category.getId());

        if (hasChildren) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        category.delete();
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.CATEGORY_NOT_FOUND,
                "categoryId=" + categoryId
            ));
    }

    private Category findParent(Long parentId) {
        if (parentId == null) {
            return null;
        }

        return getCategory(parentId);
    }

    private void validateDuplicate(
        String name,
        TransactionType type,
        Long parentId,
        Long excludeCategoryId
    ) {
        boolean exists = categoryRepository.existsDuplicateName(
            name,
            type,
            parentId,
            excludeCategoryId
        );

        if (exists) {
            throw new BusinessException(
                ErrorCode.CATEGORY_ALREADY_EXISTS,
                "name=" + name + ", type=" + type
            );
        }
    }
}
