package com.supernovapos.finalproject.auth.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "所有角色與其權限清單回應")
public class RolesWithPermissionsResponse {

    @Schema(description = "角色與其綁定的權限清單")
    private List<RoleWithPermissions> roles;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "角色與權限")
    public static class RoleWithPermissions {

        @Schema(example = "ROLE_OWNER")
        private String roleCode;
        
        @Schema(example = "STORE")
        private String category;

        @Schema(example = "[\"STORE_READ\", \"STORE_UPDATE\"]")
        private List<String> permissions;
    }
    
}
