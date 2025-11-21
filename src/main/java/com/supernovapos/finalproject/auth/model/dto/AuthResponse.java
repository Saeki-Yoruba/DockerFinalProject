package com.supernovapos.finalproject.auth.model.dto;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登入成功回傳的資訊")
public class AuthResponse {

    @Schema(description = "JWT 驗證用 Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "使用者 ID", example = "101")
    private Long id;
    
    @Schema(description = "使用者信箱", example = "staff@example.com")
    private String email;

    @Schema(description = "使用者暱稱", example = "員工")
    private String nickname;

    @Schema(description = "頭像圖片", example = "/images/default.png")
    private String avatar;

    @Schema(description = "目前可用點數", example = "200")
    private Integer point;

    @Schema(description = "使用者角色代碼清單", example = "[\"ROLE_STAFF\"]")
    private List<String> roles;

    @Schema(description = "角色大類，用來判斷系統進入點 (ADMIN / STORE / USER)", 
            example = "[\"STORE\"]")
    private List<String> roleCategories;
}
