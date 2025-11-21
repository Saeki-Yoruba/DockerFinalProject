package com.supernovapos.finalproject.common.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Schema(description = "錯誤回應格式")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
	
    @Schema(description = "發生時間", example = "2025-09-11T20:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP 狀態碼", example = "403")
    private int status;

    @Schema(description = "錯誤簡述", example = "權限不足")
    private String error;

    @Schema(description = "詳細訊息", example = "您沒有權限執行此操作")
    private Object message;
    
}
