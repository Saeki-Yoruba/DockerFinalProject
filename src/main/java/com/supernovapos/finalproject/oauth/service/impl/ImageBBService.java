package com.supernovapos.finalproject.oauth.service.impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.supernovapos.finalproject.common.exception.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageBBService {

    @Value("${IMAGEBB_API_KEY}")
    private String apiKey;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String ALLOWED_EXT = ".*\\.(jpg|jpeg|png|webp)$";

    public String uploadImage(MultipartFile file) throws IOException {
        checkApiKey();
        validateFile(file);

        HttpEntity<MultiValueMap<String, Object>> request = buildRequest(file);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.imgbb.com/1/upload?key=" + apiKey,
                request,
                Map.class
        );

        return extractImageUrl(response);
    }

    // ----------------- Private Helpers -----------------

    /** 檢查 API Key 是否設定 */
    private void checkApiKey() {
        if ("dummy-key".equals(apiKey)) {
            throw new IllegalStateException("ImageBB API Key 未設定，請確認環境變數");
        }
    }

    /** 驗證檔案格式與大小 */
    private void validateFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.matches(ALLOWED_EXT)) {
            throw new InvalidRequestException("只允許 jpg/jpeg/png/webp 格式");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidRequestException("檔案大小不可超過 5MB");
        }
    }

    /** 建立 HTTP 請求 */
    private HttpEntity<MultiValueMap<String, Object>> buildRequest(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        return new HttpEntity<>(body, headers);
    }

    /** 解析回傳 JSON 並取出圖片 URL */
    private String extractImageUrl(ResponseEntity<Map> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("上傳失敗，HTTP 狀態碼: " + response.getStatusCode());
        }

        Map<String, Object> bodyMap = response.getBody();
        if (bodyMap == null || !Boolean.TRUE.equals(bodyMap.get("success"))) {
            throw new RuntimeException("上傳失敗，回傳格式異常");
        }

        Map<String, Object> data = (Map<String, Object>) bodyMap.get("data");
        return (String) data.get("url");
    }
}
