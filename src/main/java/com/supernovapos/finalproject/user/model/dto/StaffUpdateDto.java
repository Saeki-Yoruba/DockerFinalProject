package com.supernovapos.finalproject.user.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "員工更新請求 DTO")
public class StaffUpdateDto {

    @Schema(description = "新密碼（至少 6 碼，選填）", example = "newPassword123")
    @Size(min = 6, max = 30, message = "密碼長度需為 6 ~ 30 碼")
    private String password;

    @Schema(description = "暱稱（選填）", example = "小王")
    @Size(max = 50, message = "暱稱長度不能超過 50")
    private String nickname;

    @Schema(description = "頭像 URL（選填）", example = "/images/avatar123.png")
    private String avatar;

    @Schema(description = "角色代碼清單（選填，如 ROLE_STAFF、ROLE_COOK）", 
            example = "[\"ROLE_STAFF\", \"ROLE_COOK\"]")
    private List<String> roles;
}