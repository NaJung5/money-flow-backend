package com.njung.moneyflow.fixture;

import com.njung.moneyflow.transaction.entity.Transaction;
import com.njung.moneyflow.transaction.entity.TransactionType;

import java.time.LocalDate;
import java.time.ZoneId;

public class TransactionFixture {

    public static Transaction createTransaction() {
        return new Transaction(
            TransactionType.EXPENSE,
            MoneyFixture.createMoney(),
            LocalDate.now(ZoneId.of("Asia/Seoul")),
            CategoryFixture.createExpenseCategory(),
            "점심",
            "식당"
        );
    }
}
