package com.njung.moneyflow.category.entity;

import com.njung.moneyflow.fixture.CategoryFixture;
import com.njung.moneyflow.transaction.entity.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    @Test
    @DisplayName("카테고리 정상 생성")
    void createCategory() {
        Category category = CategoryFixture.createExpenseCategory();

        assertThat(category.getName()).isEqualTo("SampleCategory");
        assertThat(category.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(category.getCategoryType()).isEqualTo(CategoryType.CUSTOM);
        assertThat(category.isActive()).isTrue();
        assertThat(category.getParentCategory()).isNull();

    }

    @Test
    @DisplayName("자식 카테고리 정상 생성")
    void createChildCategory() {
        Category childCategory = createSampleChildCategory();

        assertThat(childCategory.getName()).isEqualTo("자식 카테고리");
        assertThat(childCategory.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(childCategory.getCategoryType()).isEqualTo(CategoryType.CUSTOM);

        Category parentCategory = childCategory.getParentCategory();

        assertThat(parentCategory.getName()).isEqualTo("부모 카테고리");
        assertThat(parentCategory.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(parentCategory.getCategoryType()).isEqualTo(CategoryType.CUSTOM);
        assertThat(parentCategory.getParentCategory()).isNull();
    }

    @Test
    @DisplayName("카테고리명 변경 정상 동작")
    void changeCategoryName() {
        Category category = CategoryFixture.createExpenseCategory();
        category.rename("카테고리명 변경");

        assertThat(category.getName()).isEqualTo("카테고리명 변경");
    }


    @Test
    @DisplayName("자기 자신을 부모로 설정하면 실패")
    void failWhenCategorySetsItselfAsParent() {
        Category category = CategoryFixture.createExpenseCategory();

        assertThatThrownBy(() -> category.changeParentCategory(category)).isInstanceOf(
            IllegalArgumentException.class);
    }

    @Test
    @DisplayName("자식과 부모의 거래 타입이 다르면 실패")
    void failWhenCategoryTypeNotMatch() {
        Category parentCategory = CategoryFixture.createExpenseCategory();

        assertThatThrownBy(() -> new Category(
            "자식 카테고리",
            TransactionType.INCOME,
            CategoryType.CUSTOM,
            parentCategory
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("카테고리는 3뎁스까지 만들 수 없음")
    void failWhenCreatingThirdDepthCategory() {
        Category childCategory = createSampleChildCategory();

        assertThatThrownBy(() -> new Category(
            "자식카테고리2",
            TransactionType.EXPENSE,
            CategoryType.CUSTOM,
            childCategory
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("카테고리는 최대 2단계까지만 생성할 수 있습니다.");
    }

    @Test
    @DisplayName("삭제된 카테고리는 부모 카테고리로 사용 불가능")
    void failWhenDeletedCategory() {
        Category category = CategoryFixture.createExpenseCategory();
        category.requestDelete();

        assertThatThrownBy(() -> new Category(
            "자식 카테고리",
            TransactionType.EXPENSE,
            CategoryType.CUSTOM,
            category
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("기본제공 카테고리명은 수정이 불가능 합니다.")
    void failWhenSystemCategoryChange() {
        Category category = new Category(
            "기본제공 카테고리",
            TransactionType.INCOME,
            CategoryType.SYSTEM,
            null
        );

        assertThatThrownBy(() -> category.rename("카테고리명 변경")).isInstanceOf(IllegalStateException.class);

    }


    private Category createSampleChildCategory() {
        Category parentCategory = new Category(
            "부모 카테고리",
            TransactionType.EXPENSE,
            CategoryType.CUSTOM,
            null
        );

        return new Category(
            "자식 카테고리",
            TransactionType.EXPENSE,
            CategoryType.CUSTOM,
            parentCategory
        );
    }

}
