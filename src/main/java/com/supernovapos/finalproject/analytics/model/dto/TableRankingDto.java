package com.supernovapos.finalproject.analytics.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "桌位營收排行資訊")
public class TableRankingDto {

    @Schema(description = "桌號 ID", example = "3")
    private Integer tableId;

    @Schema(description = "該桌營收總額", example = "12000")
    private Long revenue;
}
