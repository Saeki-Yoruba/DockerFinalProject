package com.supernovapos.finalproject.oauth.verifier;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleIdToken.Payload verify(String idToken) {
        try {
            // 呼叫 Google 驗證端點
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            String response = restTemplate.getForObject(url, String.class);

            JsonNode json = objectMapper.readTree(response);

            if (json.has("error")) {
                throw new InvalidRequestException("Google Token 無效");
            }

            // 模擬 Payload
            GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
            payload.set("email", json.get("email").asText());
            payload.set("name", json.get("name").asText());
            payload.set("picture", json.get("picture").asText());
            payload.setSubject(json.get("sub").asText());
            return payload;
        } catch (Exception e) {
            throw new InvalidRequestException("Google Token 驗證失敗");
        }
    }
}
