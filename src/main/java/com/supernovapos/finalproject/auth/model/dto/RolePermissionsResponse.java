package com.supernovapos.finalproject.auth.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色權限覆蓋回應")
public class RolePermissionsResponse {

    @Schema(description = "角色代碼", example = "ROLE_OWNER")
    private String role;

    @Schema(description = "綁定的權限代碼清單", example = "[\"STORE_READ\", \"STORE_UPDATE\"]")
    private List<String> permissions;
}
