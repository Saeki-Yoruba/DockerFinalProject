package com.supernovapos.finalproject.analytics.model.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "顧客消費 KPI 總覽資料")
public class UserSpendingSummaryDto {

    @Schema(description = "總會員數（符合篩選條件的顧客數量）", example = "1200")
    private int totalUsers;

    @Schema(description = "總消費金額", example = "350000.75")
    private BigDecimal totalRevenue;

    @Schema(description = "平均客單價（總消費金額 ÷ 顧客數量）", example = "291.67")
    private BigDecimal averageOrderValue;

    @Schema(description = "活躍會員數（最近一個月內有消費紀錄的顧客數量）", example = "320")
    private long activeUsers;
}

