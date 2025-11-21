package com.supernovapos.finalproject.oauth.dto;

import lombok.Data;

@Data
public class LineTokenRequest {
    private String accessToken; // 前端傳來的 Line access_token
}
