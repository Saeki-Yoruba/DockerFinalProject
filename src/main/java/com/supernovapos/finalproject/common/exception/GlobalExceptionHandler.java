package com.supernovapos.finalproject.common.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.supernovapos.finalproject.common.model.AuthErrorResponse;
import com.supernovapos.finalproject.common.model.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理通用業務異常 (ResourceNotFound, Conflict, InvalidRequest...)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * 處理非法參數
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 處理驗證錯誤 (@Valid 驗證失敗)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 處理登入/驗證錯誤 (AuthException 帶有 code)
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthException(AuthException ex) {
        AuthErrorResponse response = AuthErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus())
                .error(HttpStatus.valueOf(ex.getStatus()).getReasonPhrase())
                .message(ex.getMessage())
                .code(ex.getCode())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler({ DisabledException.class })
    public ResponseEntity<AuthErrorResponse> handleDisabled(DisabledException ex) {
        throw new AuthException("帳號尚未驗證", "UNVERIFIED_ACCOUNT", HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler({ LockedException.class })
    public ResponseEntity<AuthErrorResponse> handleLocked(LockedException ex) {
        throw new AuthException("帳號已停用", "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN.value());
    }
    
    /**
     * 處理其他未預期的錯誤
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // 避免吃掉 Security 的 401 / 403，讓 Spring Security 自己處理
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException) {
            throw ex;
        }

        // log stack trace，方便 debug
        log.error("Unexpected error occurred", ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("系統內部發生錯誤，請聯絡管理員")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
