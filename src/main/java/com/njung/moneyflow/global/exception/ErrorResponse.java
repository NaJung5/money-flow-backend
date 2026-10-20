package com.njung.moneyflow.global.exception;

public record ErrorResponse(
    String message,
    int status
) {
}
