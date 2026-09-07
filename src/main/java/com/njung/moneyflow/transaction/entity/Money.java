package com.njung.moneyflow.transaction.entity;

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
            throw new IllegalArgumentException("amount는 0보다 커야 합니다.");
        }
        this.amount = amount;
    }

}
