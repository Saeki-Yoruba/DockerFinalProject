package com.supernovapos.finalproject.common.exception;

import org.springframework.http.HttpStatus;

//通用業務異常
public class BusinessException extends RuntimeException {
	 // 序列化版本號
	private static final long serialVersionUID = 1L;
	
    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
