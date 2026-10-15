package com.njung.moneyflow.fixture;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.entity.CategoryType;
import com.njung.moneyflow.transaction.entity.TransactionType;

public class CategoryFixture {

    public static Category createExpenseCategory(String categoryName) {
        return new Category(
            categoryName,
            TransactionType.EXPENSE,
            CategoryType.CUSTOM,
            null
        );
    }

    public static Category createSystemCategory(String categoryName) {
        return new Category(
            categoryName,
            TransactionType.EXPENSE,
            CategoryType.SYSTEM,
            null
        );
    }
}
