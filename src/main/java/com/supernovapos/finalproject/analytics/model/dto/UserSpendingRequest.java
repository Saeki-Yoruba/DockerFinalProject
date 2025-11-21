package com.supernovapos.finalproject.analytics.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "顧客消費查詢條件")
public class UserSpendingRequest {

    @Schema(description = "關鍵字搜尋（暱稱或 Email 部分匹配）", example = "小明")
    private String keyword;

    @Schema(description = "最低消費金額（過濾總消費低於此值的顧客）", example = "1000")
    private BigDecimal minSpent;

    @Schema(description = "查詢開始日期", example = "2025-09-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "查詢結束日期", example = "2025-09-23")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "分頁頁碼（從 0 開始）", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Schema(description = "每頁筆數", example = "10", defaultValue = "10")
    private Integer size = 10;

    @Schema(description = "排序欄位，可選: userId, nickname, email, totalSpent, orderCount, lastOrderDate", 
            example = "totalSpent", defaultValue = "totalSpent")
    private String sortBy = "totalSpent";

    @Schema(description = "排序方向，可選: ASC / DESC", example = "DESC", defaultValue = "DESC")
    private String direction = "DESC";
}
