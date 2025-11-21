package com.supernovapos.finalproject.common.exception;

public class GoogleAuthException extends AuthException {
    public GoogleAuthException(String message) {
        super(message, "GOOGLE_AUTH_ERROR", 401);
    }

    public GoogleAuthException(String message, Throwable cause) {
        super(message, "GOOGLE_AUTH_ERROR", 401);
        initCause(cause);
    }
}
