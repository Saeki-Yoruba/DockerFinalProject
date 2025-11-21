package com.supernovapos.finalproject.auth.model.dto;

import com.supernovapos.finalproject.auth.constant.RoleCategoryEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "角色資訊回應")
public class RoleResponse {

    @Schema(description = "角色 ID", example = "1")
    private Integer id;

    @Schema(description = "角色代碼", example = "ROLE_ADMIN")
    private String code;

    @Schema(description = "角色名稱", example = "系統管理員")
    private String name;

    @Schema(description = "角色分類", example = "ADMIN")
    private RoleCategoryEnum category;
    
    @Schema(description = "是否啟用", example = "true")
    private Boolean isAvailable;
}
