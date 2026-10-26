package com.njung.moneyflow.category.service;

import com.njung.moneyflow.category.dto.CategoryRequest;
import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.repository.CategoryRepository;
import com.njung.moneyflow.fixture.CategoryFixture;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
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

    // ==================== 생성 테스트 ====================

    @Test
    @DisplayName("부모 카테고리를 지정하면 자식 카테고리가 생성된다")
    void shouldCreateCategoryWithParent() {
        // given
        Category parent = CategoryFixture.createExpenseCategory("부모 카테고리");

        categoryRepository.save(parent);

        CategoryRequest request = new CategoryRequest(
            "자식 카테고리",
            TransactionType.EXPENSE,
            parent.getId()
        );

        // when
        Long childId = categoryService.create(request);
        Category child = categoryRepository.findById(childId).orElseThrow();

        // then
        assertThat(child.getName()).isEqualTo("자식 카테고리");
        assertThat(child.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(child.getParentCategory().getId()).isEqualTo(parent.getId());
    }


    @Test
    @DisplayName("존재하지 않는 부모 카테고리를 지정하면 예외가 발생한다")
    void failWhenParentCategoryDoesNotExist() {
        // given
        CategoryRequest request = new CategoryRequest("자식 카테고리", TransactionType.EXPENSE, 9999L);

        // when & then
        assertThatThrownBy(() -> categoryService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("동일한 부모 아래 같은 이름과 거래 유형의 카테고리를 생성하면 예외가 발생한다")
    void failWhenDuplicateChildCategoryExists() {
        // given
        Category parent = CategoryFixture.createExpenseCategory("부모 카테고리");

        categoryRepository.save(parent);

        CategoryRequest request = new CategoryRequest(
            "자식 카테고리",
            TransactionType.EXPENSE,
            parent.getId()
        );

        categoryService.create(request);

        // when & then
        assertThatThrownBy(() -> categoryService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_ALREADY_EXISTS);
    }

    // ==================== 삭제 테스트 ====================

    @Test
    @DisplayName("카테고리를 삭제하면 삭제 일시가 설정된다")
    void shouldSoftDeleteCategory() {
        // given
        Category category = CategoryFixture.createExpenseCategory("SampleCategory");

        categoryRepository.save(category);

        // when
        categoryService.delete(category.getId());

        // then
        assertThat(category.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("자식 카테고리가 모두 삭제된 경우 부모 카테고리를 삭제할 수 있다")
    void shouldDeleteParentWhenAllChildrenDeleted() {
        // given
        Category parent = CategoryFixture.createExpenseCategory("부모 카테고리");

        categoryRepository.save(parent);

        CategoryRequest request = new CategoryRequest(
            "자식 카테고리",
            TransactionType.EXPENSE,
            parent.getId()
        );

        Long childCategoryId = categoryService.create(request);
        Category childCategory = categoryRepository.findById(childCategoryId).orElseThrow();

        categoryService.delete(childCategory.getId());

        // when
        categoryService.delete(parent.getId());

        // then
        assertThat(childCategory.getDeletedAt()).isNotNull();
        assertThat(parent.getDeletedAt()).isNotNull();
    }


    @Test
    @DisplayName("SYSTEM 카테고리를 삭제하려고 하면 예외가 발생한다")
    void failWhenDeletingSystemCategory() {
        // given
        Category category = CategoryFixture.createSystemCategory("SampleCategory");

        categoryRepository.save(category);

        // when & then
        assertThatThrownBy(() -> categoryService.delete(category.getId()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_SYSTEM_DELETE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("이미 삭제된 카테고리를 다시 삭제하면 카테고리를 찾을 수 없다는 예외가 발생한다")
    void failWhenDeletingAlreadyDeletedCategory() {
        // given
        Category category = CategoryFixture.createExpenseCategory("SampleCategory");

        categoryRepository.save(category);
        categoryService.delete(category.getId());

        // when & then
        assertThatThrownBy(() -> categoryService.delete(category.getId()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제되지 않은 자식 카테고리가 존재하면 부모 카테고리 삭제 시 예외가 발생한다")
    void failWhenDeletingCategoryWithChildren() {
        // given
        Category parent = CategoryFixture.createExpenseCategory("부모 카테고리");

        categoryRepository.save(parent);

        CategoryRequest request = new CategoryRequest(
            "자식 카테고리",
            TransactionType.EXPENSE,
            parent.getId()
        );

        categoryService.create(request);

        // when & then
        assertThatThrownBy(() -> categoryService.delete(parent.getId()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_HAS_CHILDREN);
    }

    @Test
    @DisplayName("하위 카테고리가 있는 SYSTEM 카테고리를 삭제하면 SYSTEM 삭제 불가 예외가 먼저 발생한다")
    void failWithSystemErrorWhenDeletingSystemCategoryWithChildren() {
        // given
        Category parent = CategoryFixture.createSystemCategory("부모 카테고리");

        categoryRepository.save(parent);

        CategoryRequest request = new CategoryRequest(
            "자식 카테고리",
            TransactionType.EXPENSE,
            parent.getId()
        );

        categoryService.create(request);

        // when & then
        assertThatThrownBy(() -> categoryService.delete(parent.getId()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_SYSTEM_DELETE_NOT_ALLOWED);
    }

}
