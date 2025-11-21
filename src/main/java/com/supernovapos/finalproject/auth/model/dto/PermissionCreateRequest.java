package com.supernovapos.finalproject.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "新增權限請求")
public class PermissionCreateRequest {

    @Schema(description = "分類 ID", example = "1")
    private Integer categoryId;

    @Schema(description = "權限代碼", example = "STORE_READ")
    private String code;

    @Schema(description = "HTTP 方法", example = "GET")
    private String httpMethod;

    @Schema(description = "對應的 URL", example = "/api/store")
    private String url;

    @Schema(description = "描述", example = "查詢店家資訊")
    private String description;

    @Schema(description = "是否啟用", example = "true")
    private Boolean isAvailable = true;
}
