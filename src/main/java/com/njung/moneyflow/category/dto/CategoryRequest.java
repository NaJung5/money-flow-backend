package com.njung.moneyflow.category.dto;

import com.njung.moneyflow.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank
    @Size(max = 50)
    String name,

    @NotNull
    TransactionType type,

    Long parentId
) {
}
