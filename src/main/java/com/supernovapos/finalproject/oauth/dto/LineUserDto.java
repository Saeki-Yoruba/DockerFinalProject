package com.supernovapos.finalproject.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineUserDto {
    private String lineUid;
    private String name;
    private String avatar;
    private String accessToken;
    private String idToken;
}
