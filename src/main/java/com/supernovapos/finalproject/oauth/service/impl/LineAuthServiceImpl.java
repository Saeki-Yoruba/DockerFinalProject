package com.supernovapos.finalproject.oauth.service.impl;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supernovapos.finalproject.common.exception.LineAuthException;
import com.supernovapos.finalproject.oauth.config.LineProperties;
import com.supernovapos.finalproject.oauth.dto.LineUserDto;
import com.supernovapos.finalproject.oauth.service.LineAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LineAuthServiceImpl implements LineAuthService {

    private final LineProperties lineProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public String buildAuthUrl(String mode) {
        return String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&state=%s&scope=%s",
                lineProperties.getAuthUrl(),
                lineProperties.getClientId(),
                lineProperties.getRedirect(),
                mode,
                lineProperties.getScope().replace(" ", "%20"));
    }

    @Override
    public LineUserDto handleCallback(String code) {
        try {
            log.info("Handling LINE OAuth callback with code={}", code);

            // 1. 換取 Token
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("code", code);
            formData.add("redirect_uri", lineProperties.getRedirect());
            formData.add("client_id", lineProperties.getClientId());
            formData.add("client_secret", lineProperties.getSecret());

            JsonNode tokenJson = postForJson(lineProperties.getTokenUrl(), formData);
            String accessToken = tokenJson.path("access_token").asText(null);
            String idToken = tokenJson.path("id_token").asText(null);

            if (accessToken == null) {
                throw new LineAuthException("LINE accessToken is null");
            }

            // 2. 呼叫 User Profile API
            JsonNode profileJson = getForJson(lineProperties.getProfileUrl(), accessToken);

            String lineUid = profileJson.path("userId").asText(null);
            String name = profileJson.path("displayName").asText(null);
            String avatar = profileJson.path("pictureUrl").asText(null);

            if (lineUid == null) {
                throw new LineAuthException("LINE userId missing in profile response");
            }

            LineUserDto userDto = new LineUserDto(lineUid, name, avatar, accessToken, idToken);
            log.info("LINE login success: lineUid={}, name={}", lineUid, name);
            return userDto;

        } catch (Exception e) {
            log.error("LINE callback 驗證失敗", e);
            throw new LineAuthException("LINE callback 驗證失敗", e);
        }
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
            log.error("LINE POST 請求失敗 url={}", url, e);
            throw new LineAuthException("LINE POST 請求失敗: " + url, e);
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
            log.error("LINE GET 請求失敗 url={}", url, e);
            throw new LineAuthException("LINE GET 請求失敗: " + url, e);
        }
    }
}

