package com.supernovapos.finalproject.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新權限請求")
public class PermissionUpdateRequest {

    @Schema(description = "分類 ID", example = "1")
    private Integer categoryId;

    @Schema(description = "HTTP 方法", example = "PUT")
    private String httpMethod;

    @Schema(description = "對應的 URL", example = "/api/store/{id}")
    private String url;

    @Schema(description = "描述", example = "更新店家資訊")
    private String description;

    @Schema(description = "是否啟用", example = "true")
    private Boolean isAvailable = true;
}
