package com.supernovapos.finalproject.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleUserDto {
    private String email;
    private String name;
    private String googleUid;
    private String idToken;
    private String accessToken;
}