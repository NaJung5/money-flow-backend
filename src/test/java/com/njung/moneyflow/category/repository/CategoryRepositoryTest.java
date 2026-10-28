package com.njung.moneyflow.category.repository;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("관리용 조회는 비활성 카테고리를 포함하고 삭제된 카테고리는 제외한다")
    void findAllByDeletedAtIsNull() {
        // given
        Category activeCategory = CategoryFixture.createExpenseCategory("ActiveCategory");

        Category inactiveCategory = CategoryFixture.createExpenseCategory("InactiveCategory");

        Category deletedCategory = CategoryFixture.createExpenseCategory("DeletedCategory");

        inactiveCategory.deactivate();
        deletedCategory.delete();

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
        deletedCategory.delete();

        categoryRepository.saveAll(List.of(activeCategory, inactiveCategory, deletedCategory));

        // when
        List<Category> actual = categoryRepository.findAllByActiveTrueAndDeletedAtIsNull();

        // then
        assertThat(actual).containsExactly(activeCategory);
    }
}
