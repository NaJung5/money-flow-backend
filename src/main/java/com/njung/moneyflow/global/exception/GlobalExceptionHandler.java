package com.njung.moneyflow.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException exception
    ) {
        ErrorResponse response = ErrorResponse.of(
            exception.getErrorCode(),
            exception.getMessage()
        );

        return ResponseEntity
            .status(response.status())
            .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception
    ) {
        List<ErrorResponse.FieldErrorResponse> fieldErrors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> new ErrorResponse.FieldErrorResponse(
                fieldError.getField(),
                fieldError.getDefaultMessage()
            ))
            .toList();

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, fieldErrors);

        return ResponseEntity
            .status(response.status())
            .body(response);
    }
}
