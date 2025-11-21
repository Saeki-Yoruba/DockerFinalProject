package com.supernovapos.finalproject.common.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final String code;
    private final int status;

    public AuthException(String message, String code, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
