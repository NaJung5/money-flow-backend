package com.njung.moneyflow.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "올바르지 못한 입력값 입니다."),

    CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 카테고리입니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

    CATEGORY_DELETED(HttpStatus.CONFLICT, "이미 삭제된 카테고리입니다."),

    CATEGORY_SYSTEM_DELETE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "시스템 카테고리는 삭제할 수 없습니다."),

    CATEGORY_HAS_CHILDREN(HttpStatus.CONFLICT, "하위 카테고리가 존재하여 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
