package com.njung.moneyflow.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException exception
    ) {
        log.warn("[비즈니스 예외] {} - {}", exception.getErrorCode(), exception.getMessage());

        ErrorResponse response = ErrorResponse.of(exception.getErrorCode());

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

        log.warn(
            "[입력값 검증 실패] fields={}",
            fieldErrors.stream().map(ErrorResponse.FieldErrorResponse::field).toList()
        );

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, fieldErrors);

        return ResponseEntity
            .status(response.status())
            .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException exception
    ) {
        log.warn("[요청 파싱 실패] {}", exception.getMessage());

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT);

        return ResponseEntity
            .status(response.status())
            .body(response);
    }
}
