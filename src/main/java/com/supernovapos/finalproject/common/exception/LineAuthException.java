package com.supernovapos.finalproject.common.exception;

public class LineAuthException extends AuthException {

    public LineAuthException(String message) {
        super(message, "LINE_AUTH_ERROR", 401);
    }

    public LineAuthException(String message, Throwable cause) {
        super(message, "LINE_AUTH_ERROR", 401);
        initCause(cause);
    }
}
