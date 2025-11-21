package com.supernovapos.finalproject.auth.model.dto;

import com.supernovapos.finalproject.auth.constant.RoleCategoryEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "建立角色請求")
public class RoleCreateRequest {
    @Schema(example = "ROLE_COOK")
    private String code;

    @Schema(example = "廚師")
    private String name;

    @Schema(example = "STORE")
    private RoleCategoryEnum category;
}
