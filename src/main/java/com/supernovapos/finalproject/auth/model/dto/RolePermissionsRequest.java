package com.supernovapos.finalproject.auth.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色權限覆蓋請求")
public class RolePermissionsRequest {

    @NotBlank
    @Schema(
        description = "角色代碼",
        example = "ROLE_OWNER",
        allowableValues = {"ROLE_ADMIN", "ROLE_OWNER", "ROLE_STAFF", "ROLE_USER"}
    )
    private String roleCode;

    @NotEmpty
    @Schema(
        description = "要綁定的權限代碼清單",
        example = "[\"STORE_READ\", \"STORE_UPDATE\"]"
    )
    private List<@NotBlank String> permissions;
}
