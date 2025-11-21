package com.supernovapos.finalproject.analytics.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "營收趨勢回應 DTO")
public class RevenueTrendResponse {

    @Schema(description = "X 軸標籤（日期 / 月份 / 年份）", 
            example = "[\"09-12\", \"09-13\", \"09-14\", \"09-15\", \"09-16\", \"09-17\", \"09-18\"]")
    private List<String> labels;

    @Schema(description = "Y 軸數值（對應營收）", 
            example = "[1200, 5400, 3200, 4500, 6100, 3000, 7200]")
    private List<Long> values;
}
