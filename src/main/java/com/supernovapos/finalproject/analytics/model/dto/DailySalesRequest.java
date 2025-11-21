package com.supernovapos.finalproject.analytics.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "每日銷售報表查詢條件")
public class DailySalesRequest {

    @Schema(description = "查詢模式: day=單日, range=區間, month=月份, year=年度", 
            example = "day")
    private String mode;

    @Schema(description = "查詢日期 (當 mode=day 時使用)", 
            example = "2025-09-23")
    private String date;

    @Schema(description = "查詢區間開始日期 (當 mode=range 時使用)", 
            example = "2025-09-01")
    private String startDate;

    @Schema(description = "查詢區間結束日期 (當 mode=range 時使用)", 
            example = "2025-09-23")
    private String endDate;

    @Schema(description = "查詢月份 (當 mode=month 時使用, 格式: yyyy-MM)", 
            example = "2025-09")
    private String month;

    @Schema(description = "查詢年份 (當 mode=year 時使用, 格式: yyyy)", 
            example = "2025")
    private String year;

    @Schema(description = "分頁頁碼 (從 0 開始)", example = "0")
    private Integer page;

    @Schema(description = "每頁筆數", example = "10")
    private Integer size;

    @Schema(description = "排序欄位，可選: orderDate, dailyRevenue, orderCount, avgOrderValue", 
            example = "orderDate")
    private String sortBy;

    @Schema(description = "排序方向，可選: ASC 或 DESC", 
            example = "DESC")
    private String sortOrder;
}
