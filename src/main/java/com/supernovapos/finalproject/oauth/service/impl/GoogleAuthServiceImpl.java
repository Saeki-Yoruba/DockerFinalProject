package com.supernovapos.finalproject.oauth.service.impl;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supernovapos.finalproject.common.exception.GoogleAuthException;
import com.supernovapos.finalproject.oauth.config.GoogleProperties;
import com.supernovapos.finalproject.oauth.dto.GoogleTokenDto;
import com.supernovapos.finalproject.oauth.dto.GoogleUserDto;
import com.supernovapos.finalproject.oauth.service.GoogleAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final GoogleProperties googleProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /**
     * 建立 Google OAuth 登入 URL
     *
     * @param mode 透過 state 傳遞的模式（例如 login / bind 等）
     * @return 可供前端跳轉的完整 Google 登入連結
     */
    @Override
    public String buildAuthUrl(String mode) {
        return String.format(
                "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&prompt=select_account",
                googleProperties.getAuthUrl(),
                googleProperties.getClientId(),
                googleProperties.getRedirect(),
                googleProperties.getScope().replace(" ", "%20"),
                mode
        );
    }

    /**
     * Google OAuth 回調處理：
     *  1. 透過授權碼換取 Token
     *  2. 取得使用者資訊
     *  3. 封裝成 DTO 回傳
     *
     * @param code Google OAuth 授權碼
     * @return Google 使用者資料（封裝於 DTO）
     */
    @Override
    public GoogleUserDto handleCallback(String code) {
        try {
            log.info("Handling Google OAuth callback with code={}", code);

            // 1. 一次取回 access_token / id_token
            GoogleTokenDto tokenDto = fetchTokens(code);

            // 2. 呼叫 Google UserInfo API
            JsonNode userJson = fetchUserInfo(tokenDto.getAccessToken());

            // 3. 轉換為 DTO
            GoogleUserDto userDto = mapToDto(userJson, tokenDto);

            log.info("Google login success: email={}, googleUid={}",
                    userDto.getEmail(), userDto.getGoogleUid());
            return userDto;

        } catch (Exception e) {
            log.error("Google callback 驗證失敗", e);
            throw new GoogleAuthException("Google callback 驗證失敗", e);
        }
    }

    /**
     * 用授權碼呼叫 Google Token API，換取 Token 套件
     */
    private GoogleTokenDto fetchTokens(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getSecret());
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", googleProperties.getRedirect());

        JsonNode tokenJson = postForJson(googleProperties.getTokenUrl(), formData);

        String accessToken = tokenJson.path("access_token").asText(null);
        String idToken = tokenJson.path("id_token").asText(null);

        if (accessToken == null) {
            throw new GoogleAuthException("Google accessToken is null");
        }

        return new GoogleTokenDto(
                accessToken,
                idToken,
                tokenJson.path("refresh_token").asText(null),
                tokenJson.path("token_type").asText(null),
                tokenJson.path("expires_in").asLong(0)
        );
    }

    /**
     * 呼叫 Google UserInfo API，取得使用者資訊
     */
    private JsonNode fetchUserInfo(String accessToken) {
        return getForJson(googleProperties.getUserinfoUrl(), accessToken);
    }

    /**
     * 將 Google UserInfo JSON 轉換成 DTO
     */
    private GoogleUserDto mapToDto(JsonNode userJson, GoogleTokenDto tokenDto) {
        String email = userJson.path("email").asText(null);
        String name = userJson.path("name").asText(null);
        String googleUid = userJson.path("id").asText(null);

        if (email == null || googleUid == null) {
            throw new GoogleAuthException("Google user info is incomplete: " + userJson);
        }
        return new GoogleUserDto(email, name, googleUid,
                tokenDto.getIdToken(), tokenDto.getAccessToken());
    }

    /** 共用 POST 請求 */
    private JsonNode postForJson(String url, MultiValueMap<String, String> body) {
        try {
            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(response);
        } catch (Exception e) {
            log.error("POST 請求失敗 url={}", url, e);
            throw new GoogleAuthException("POST 請求失敗: " + url, e);
        }
    }

    /** 共用 GET 請求 */
    private JsonNode getForJson(String url, String accessToken) {
        try {
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(response);
        } catch (Exception e) {
            log.error("GET 請求失敗 url={}", url, e);
            throw new GoogleAuthException("GET 請求失敗: " + url, e);
        }
    }
}
