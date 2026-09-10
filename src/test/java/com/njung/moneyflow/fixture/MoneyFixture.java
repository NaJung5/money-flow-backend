package com.njung.moneyflow.fixture;

import com.njung.moneyflow.transaction.entity.Money;

public class MoneyFixture {

    public static Money createMoney() {
        return new Money(100000);
    }
}
