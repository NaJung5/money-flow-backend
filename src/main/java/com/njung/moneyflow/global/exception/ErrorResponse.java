package com.njung.moneyflow.global.exception;

import java.util.List;

public record ErrorResponse(
    String message,
    int status,
    List<FieldErrorResponse> fieldErrors
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getMessage(), errorCode.getStatus().value(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(
            errorCode.getMessage(),
            errorCode.getStatus().value(),
            fieldErrors
        );
    }

    public record FieldErrorResponse(
        String field,
        String message
    ) {
    }
}
