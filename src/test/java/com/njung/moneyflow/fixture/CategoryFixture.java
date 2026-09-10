package com.njung.moneyflow.fixture;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.entity.CategoryType;
import com.njung.moneyflow.transaction.entity.TransactionType;

public class CategoryFixture {

    public static Category createExpenseCategory() {
        return new Category(
            "SampleCategory",
            TransactionType.EXPENSE,
            CategoryType.CUSTOM,
            null
        );
    }
}
