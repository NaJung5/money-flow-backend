package com.njung.moneyflow.transaction.dto;

import com.njung.moneyflow.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TransactionRequest(
    @NotNull TransactionType type,
    @NotNull @Positive Integer amount,
    @NotNull LocalDate transactionDate,
    @NotNull Long categoryId,
    @Size(max = 50) String memo,
    @Size(max = 50) String place
) {
}
