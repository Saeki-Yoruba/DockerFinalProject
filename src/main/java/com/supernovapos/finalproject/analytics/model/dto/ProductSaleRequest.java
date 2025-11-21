package com.supernovapos.finalproject.analytics.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "商品銷售報表的查詢條件")
public record ProductSaleRequest(

    @Schema(description = "商品分類，例如：飲料、主餐、甜點", example = "飲料")
    String category,

    @Schema(description = "最小營收（單位：元）", example = "500")
    BigDecimal minRevenue,

    @Schema(description = "最大營收（單位：元）", example = "5000")
    BigDecimal maxRevenue,

    @Schema(description = "最小銷售數量", example = "10")
    Integer minQuantity,

    @Schema(description = "最大銷售數量", example = "100")
    Integer maxQuantity,

    @Schema(description = "排序欄位，可選值：totalRevenue, totalQuantity", example = "totalRevenue")
    String sortBy,

    @Schema(description = "排序方式，可選值：ASC, DESC", example = "DESC")
    String sortOrder,

    @Schema(description = "取前 N 筆資料，例如：5 代表只取前五名", example = "5")
    Integer top
) {}
