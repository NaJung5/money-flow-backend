package com.njung.moneyflow.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    CATEGORY_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "카테고리를 찾을 수 없습니다."
    ),

    CATEGORY_DELETED(
        HttpStatus.BAD_REQUEST,
        "삭제된 카테고리는 사용할 수 없습니다."
    );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
