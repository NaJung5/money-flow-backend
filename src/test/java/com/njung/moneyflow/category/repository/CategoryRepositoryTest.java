package com.njung.moneyflow.category.repository;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.fixture.CategoryFixture;
import com.njung.moneyflow.transaction.entity.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("동일한 이름과 거래 유형의 카테고리가 존재하면 true를 반환한다")
    void existsByNameAndType() {
        // given
        Category category = CategoryFixture.createExpenseCategory("SampleCategory");

        categoryRepository.save(category);

        // when
        boolean result = categoryRepository.existsByNameAndTypeAndParentCategoryIsNull(
            category.getName(),
            category.getType()
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("관리용 조회는 비활성 카테고리를 포함하고 삭제된 카테고리는 제외한다")
    void findAllByDeletedAtIsNull() {
        // given
        Category activeCategory = CategoryFixture.createExpenseCategory("ActiveCategory");

        Category inactiveCategory = CategoryFixture.createExpenseCategory("InactiveCategory");

        Category deletedCategory = CategoryFixture.createExpenseCategory("DeletedCategory");

        inactiveCategory.deactivate();
        deletedCategory.requestDelete();

        categoryRepository.saveAll(List.of(activeCategory, inactiveCategory, deletedCategory));

        List<Category> expected = List.of(activeCategory, inactiveCategory);

        // when
        List<Category> actual = categoryRepository.findAllByDeletedAtIsNull();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("등록용 조회는 활성 상태이며 삭제되지 않은 카테고리만 반환한다")
    void findAllByActiveTrueAndDeletedAtIsNull() {
        // given
        Category activeCategory = CategoryFixture.createExpenseCategory("ActiveCategory");

        Category inactiveCategory = CategoryFixture.createExpenseCategory("InactiveCategory");

        Category deletedCategory = CategoryFixture.createExpenseCategory("DeletedCategory");

        inactiveCategory.deactivate();
        deletedCategory.requestDelete();

        categoryRepository.saveAll(List.of(activeCategory, inactiveCategory, deletedCategory));

        // when
        List<Category> actual = categoryRepository.findAllByActiveTrueAndDeletedAtIsNull();

        // then
        assertThat(actual).containsExactly(activeCategory);
    }

    @Test
    @DisplayName("이름이 다르면 false를 반환한다")
    void returnFalseWhenNameIsDifferent() {
        // given
        Category category = CategoryFixture.createExpenseCategory("SampleCategory");

        categoryRepository.save(category);

        // when
        boolean result = categoryRepository.existsByNameAndTypeAndParentCategoryIsNull(
            "DifferentCategory",
            category.getType()
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("거래 유형이 다르면 false를 반환한다")
    void returnFalseWhenTypeIsDifferent() {
        // given
        Category category = CategoryFixture.createExpenseCategory("SampleCategory");

        categoryRepository.save(category);

        // when
        boolean result = categoryRepository.existsByNameAndTypeAndParentCategoryIsNull(
            category.getName(),
            TransactionType.INCOME
        );

        // then
        assertThat(result).isFalse();
    }
}
