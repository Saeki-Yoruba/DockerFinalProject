package com.supernovapos.finalproject.analytics.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "顧客消費資料傳輸物件")
public class UserSpendingDto {

    @Schema(description = "顧客 ID", example = "101")
    private Long userId;

    @Schema(description = "顧客暱稱", example = "小明")
    private String nickname;

    @Schema(description = "顧客 Email", example = "user101@example.com")
    private String email;

    @Schema(description = "總消費金額", example = "3580.50")
    private BigDecimal totalSpent;

    @Schema(description = "總訂單數量", example = "12")
    private Integer orderCount;

    @Schema(description = "最近一次下單時間", example = "2025-09-21T15:30:00")
    private LocalDateTime lastOrderDate;

    @Schema(description = "平均每筆訂單消費金額", example = "298.38")
    private BigDecimal avgSpent;
}
