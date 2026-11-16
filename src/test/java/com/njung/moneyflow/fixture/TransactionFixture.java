package com.njung.moneyflow.fixture;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.transaction.entity.Transaction;
import com.njung.moneyflow.transaction.entity.TransactionType;

import java.time.LocalDate;
import java.time.ZoneId;

public class TransactionFixture {

    public static Transaction createTransaction() {
        return createTransaction(CategoryFixture.createExpenseCategory("SampleCategory"));
    }

    public static Transaction createTransaction(Category category) {
        return new Transaction(
            TransactionType.EXPENSE,
            MoneyFixture.createMoney(),
            LocalDate.now(ZoneId.of("Asia/Seoul")),
            category,
            "점심",
            "식당"
        );
    }
}
