package com.supernovapos.finalproject.analytics.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "每日營收報表回傳內容")
public record DailySalesResponse(

    @Schema(description = "訂單日期", example = "2025-09-15")
    LocalDate orderDate,

    @Schema(description = "每日總營收", example = "12500")
    BigDecimal dailyRevenue,

    @Schema(description = "訂單數量", example = "32")
    Integer orderCount,

    @Schema(description = "平均客單價", example = "390.6")
    BigDecimal avgOrderValue

) {}