package com.supernovapos.finalproject.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封裝 Google OAuth Token API 的回應結果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenDto {
    private String accessToken;   // 用於呼叫 Google API 的 access_token
    private String idToken;       // JWT 格式的 id_token，可解析使用者資訊
    private String refreshToken;  // (可選) 若 scope 包含 offline_access，才會回傳
    private String tokenType;     // 通常為 "Bearer"
    private Long expiresIn;       // access_token 的有效秒數
}
