package com.njung.moneyflow.category.service;

import com.njung.moneyflow.category.dto.CategoryRequest;
import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.entity.CategoryType;
import com.njung.moneyflow.category.repository.CategoryRepository;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Long create(CategoryRequest request) {
        Category parent = findParent(request.parentId());

        validateDuplicate(request, parent);

        Category category = new Category(
            request.name(),
            request.type(),
            CategoryType.CUSTOM,
            parent
        );

        return categoryRepository.save(category).getId();
    }

    private Category findParent(Long parentId) {
        if (parentId == null) {
            return null;
        }

        return categoryRepository
            .findById(parentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.CATEGORY_NOT_FOUND,
                "parentId=" + parentId
            ));
    }

    private void validateDuplicate(CategoryRequest request, Category parent) {
        boolean exists;

        if (parent == null) {
            exists = categoryRepository.existsByNameAndTypeAndParentCategoryIsNull(
                request.name(),
                request.type()
            );
        } else {
            exists = categoryRepository.existsByNameAndTypeAndParentCategoryId(
                request.name(),
                request.type(),
                parent.getId()
            );
        }

        if (exists) {
            throw new BusinessException(
                ErrorCode.CATEGORY_ALREADY_EXISTS,
                "name=" + request.name() + ", type=" + request.type()
            );
        }
    }

}
