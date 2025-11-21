package com.supernovapos.finalproject.analytics.model.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "群組訂單報表回應")
public class GroupOrderReportDto {

    @Schema(description = "KPI 指標數據")
    private KpiDto kpi;

    @Schema(description = "時段分布（午餐 / 晚餐 / 其他）", example = "{\"午餐\": 12, \"晚餐\": 20, \"其他\": 3}")
    private Map<String, Long> periodDistribution;

    @Schema(description = "桌位營收排行榜 (Top N)")
    private List<TableRankingDto> topTables;
}