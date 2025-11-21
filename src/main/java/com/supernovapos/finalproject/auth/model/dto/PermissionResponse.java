package com.supernovapos.finalproject.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "權限資訊回應")
public class PermissionResponse {

    @Schema(description = "權限 ID", example = "101")
    private Long id;

    @Schema(description = "權限代碼 (唯一)", example = "STORE_READ")
    private String code;

    @Schema(description = "HTTP 方法", example = "GET")
    private String httpMethod;

    @Schema(description = "對應的 API URL", example = "/api/store")
    private String url;

    @Schema(description = "權限描述", example = "讀取店家資訊")
    private String description;

    @Schema(description = "是否啟用", example = "true")
    private Boolean isAvailable;

    @Schema(description = "所屬分類名稱", example = "STORE")
    private String categoryName;
}
