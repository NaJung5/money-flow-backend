package com.njung.moneyflow.category.repository;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 데이터 검증이 필요한 Repository 내용들을 작성
 */
@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("동일한 이름과 거래 유형의 카테고리가 존재하면 true를 반환한다")
    void existsByNameAndType() {
        Category category = CategoryFixture.createExpenseCategory("SampleCategory");
        categoryRepository.save(category);
        assertThat(categoryRepository.existsByNameAndType(
            category.getName(),
            category.getType()
                                                         )).isTrue();
    }

    @Test
    @DisplayName("관리용 조회는 비활성 카테고리를 포함하고 삭제된 카테고리는 제외한다")
    void findAllByDeletedAtIsNull() {
        Category activeCategory = CategoryFixture.createExpenseCategory("SampleCategory");
        Category inactiveCategory = CategoryFixture.createExpenseCategory("SampleCategory");
        Category deletedCategory = CategoryFixture.createExpenseCategory("SampleCategory");


        inactiveCategory.deactivate();
        deletedCategory.requestDelete();
        categoryRepository.saveAll(List.of(activeCategory, inactiveCategory, deletedCategory));

        List<Category> expected = List.of(activeCategory, inactiveCategory);
        List<Category> actual = categoryRepository.findAllByDeletedAtIsNull();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);

    }


}
