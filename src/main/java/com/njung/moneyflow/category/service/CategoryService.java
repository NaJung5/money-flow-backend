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
        Category parent = null;
        // 1. 중복 검증
        boolean exists = categoryRepository.existsByNameAndType(request.name(), request.type());
        if (exists) {
            throw new BusinessException(
                ErrorCode.CATEGORY_ALREADY_EXISTS,
                "name=" + request.name() + ", type=" + request.type()
            );
        }
        // 2. parentId가 있으면 부모 조회
        if (request.parentId() != null) {
            parent = categoryRepository
                .findById(request.parentId())
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND,
                    "parentId=" + request.parentId()
                ));
        }
        // 3. Category 생성
        Category category = new Category(
            request.name(),
            request.type(),
            CategoryType.CUSTOM,
            parent
        );
        // 4. save
        categoryRepository.save(category);
        // 5. id 반환

        return category.getId();
    }
}
