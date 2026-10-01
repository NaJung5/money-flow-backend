package com.njung.moneyflow.category.service;

import com.njung.moneyflow.category.dto.CategoryRequest;
import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.repository.CategoryRepository;
import com.njung.moneyflow.fixture.CategoryFixture;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.transaction.entity.TransactionType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Test
    @DisplayName("부모 카테고리를 지정하여 자식 카테고리를 생성한다")
    void createCategoryWithParent() {
        // given
        Category parent =
            CategoryFixture.createExpenseCategory("부모 카테고리");
        categoryRepository.save(parent);

        CategoryRequest request = new CategoryRequest(
            "자식 카테고리",
            TransactionType.EXPENSE,
            parent.getId()
        );

        // when
        Long childId = categoryService.create(request);

        Category child =
            categoryRepository.findById(childId).orElseThrow();

        // then
        assertThat(child.getName()).isEqualTo("자식 카테고리");
        assertThat(child.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(child.getParentCategory().getId())
            .isEqualTo(parent.getId());
    }

    @Test
    @DisplayName("존재하지 않는 부모 카테고리로 생성하면 실패한다")
    void failWhenParentCategoryDoesNotExist() {
        // given
        CategoryRequest request = new CategoryRequest(
            "자식 카테고리",
            TransactionType.EXPENSE,
            9999L
        );

        // when & then
        assertThatThrownBy(() -> categoryService.create(request))
            .isInstanceOf(BusinessException.class);
    }
}
