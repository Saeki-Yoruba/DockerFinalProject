package com.supernovapos.finalproject.oauth.verifier;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.oauth.dto.LineProfile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LineTokenVerifier {

    private final RestClient restClient = RestClient.create();

    public LineProfile verify(String accessToken) {
        try {
            String result = restClient.get()
                    .uri("https://api.line.me/v2/profile")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(result, LineProfile.class);
        } catch (Exception e) {
            throw new InvalidRequestException("LINE Token 驗證失敗");
        }
    }
}
