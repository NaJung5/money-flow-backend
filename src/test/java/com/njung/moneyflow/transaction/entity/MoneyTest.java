package com.njung.moneyflow.transaction.entity;

import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void checkAmount() {
        Money amount = new Money(1000);
        assertThat(amount.getAmount()).isEqualTo(1000);
    }

    @Test
    void failWhenAmountIsZero() {
        assertThatThrownBy(() -> new Money(0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void failWhenAmountIsNegative() {
        assertThatThrownBy(() -> new Money(-1))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT);
    }
}
