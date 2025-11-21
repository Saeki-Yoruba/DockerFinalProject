package com.supernovapos.finalproject.auth.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "權限模組 DTO")
public class PermissionCategoryDto {

	@Schema(description = "模組 ID (由系統自動生成)", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "模組名稱", example = "USER")
    private String categoryName;

    @Schema(description = "模組描述", example = "用戶相關權限模組")
    private String description;

    @Schema(description = "模組下的權限清單")
    private List<PermissionDto> permissions;
}