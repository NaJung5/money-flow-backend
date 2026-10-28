package com.njung.moneyflow.transaction.entity;

import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Money {

    @Column(nullable = false)
    private int amount;

    protected Money() {
    }

    public Money(int amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "amount=" + amount);
        }
        this.amount = amount;
    }
}
