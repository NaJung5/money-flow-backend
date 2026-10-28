package com.njung.moneyflow.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TransactionUpdateRequest(
    @NotNull @Positive Integer amount,
    LocalDate transactionDate,
    @NotNull Long categoryId,
    @Size(max = 50) String memo,
    @Size(max = 50) String place
) {
}
