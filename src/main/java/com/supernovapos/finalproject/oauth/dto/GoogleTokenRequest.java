package com.supernovapos.finalproject.oauth.dto;

import lombok.Data;

@Data
public class GoogleTokenRequest {
    private String idToken; // 前端傳來的 Google id_token
}