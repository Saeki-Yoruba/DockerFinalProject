package com.supernovapos.finalproject.analytics.model.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商品銷售報表的回傳內容")
public record ProductSaleResponse(

    @Schema(description = "商品 ID", example = "101")
    Long productId,

    @Schema(description = "商品名稱", example = "珍珠奶茶")
    String productName,

    @Schema(description = "商品分類", example = "飲料")
    String category,

    @Schema(description = "銷售總數量", example = "120")
    Integer totalQuantity,

    @Schema(description = "銷售總金額", example = "3600.50")
    BigDecimal totalRevenue

) {}