package com.supernovapos.finalproject.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "狀態更新請求")
public class StatusRequest {

    @Schema(description = "是否啟用", example = "true")
    private boolean isAvailable;
}