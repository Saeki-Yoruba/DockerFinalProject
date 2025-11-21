package com.supernovapos.finalproject.analytics.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "群組訂單 KPI 指標")
public class KpiDto {

    @Schema(description = "群組數量", example = "32")
    private Long groupCount;

    @Schema(description = "總營收金額", example = "56000")
    private Long totalRevenue;

    @Schema(description = "平均桌次金額", example = "1750.5")
    private Double avgRevenue;

    @Schema(description = "平均用餐時長 (分鐘)", example = "82.5")
    private Double avgDuration;
}
