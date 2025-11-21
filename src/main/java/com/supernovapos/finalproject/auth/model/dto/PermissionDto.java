package com.supernovapos.finalproject.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "權限 DTO")
public class PermissionDto {

    @Schema(description = "權限代碼", example = "USER_READ")
    private String code;

    @Schema(description = "HTTP 方法", example = "GET")
    private String httpMethod;

    @Schema(description = "API 路徑", example = "/api/users/**")
    private String url;

    @Schema(description = "權限描述", example = "View user information")
    private String description;

    @Schema(description = "是否啟用", example = "true")
    private Boolean isAvailable;
}
