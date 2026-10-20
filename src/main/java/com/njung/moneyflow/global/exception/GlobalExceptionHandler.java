package com.njung.moneyflow.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException exception
    ) {
        ErrorResponse response = new ErrorResponse(
            exception.getMessage(),
            exception.getErrorCode().getStatus().value()
        );

        return ResponseEntity
            .status(response.status())
            .body(response);
    }
}


