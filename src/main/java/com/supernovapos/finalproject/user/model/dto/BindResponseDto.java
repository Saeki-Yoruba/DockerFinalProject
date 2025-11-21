package com.supernovapos.finalproject.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BindResponseDto {
    private String provider;   // Google / LINE
    private String message;    // 成功 / 解除訊息
    private String uid;        // 第三方 UID
    private String email;      // 信箱（Google 綁定才有）
    private String nickname;   // 綁定後的暱稱
    private String userEmail;  // 本系統使用者 email（解除綁定才有）
}
