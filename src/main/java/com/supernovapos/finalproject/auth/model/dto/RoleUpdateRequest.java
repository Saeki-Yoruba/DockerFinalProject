package com.supernovapos.finalproject.auth.model.dto;

import com.supernovapos.finalproject.auth.constant.RoleCategoryEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新角色請求")
public class RoleUpdateRequest {
    @Schema(example = "店員")
    private String name;

    @Schema(example = "STORE")
    private RoleCategoryEnum category;
}
