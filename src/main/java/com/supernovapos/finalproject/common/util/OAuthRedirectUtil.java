package com.supernovapos.finalproject.common.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

public class OAuthRedirectUtil {

    private final String baseUrl;

    public OAuthRedirectUtil(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 導向前端頁面
     *
     * @param response   HttpServletResponse
     * @param state      狀態 (login/bind)
     * @param provider   OAuth 提供者 (google/line)
     * @param params     要帶回前端的參數 (Map<String, String>)
     */
    public void redirectToFrontend(HttpServletResponse response,
                                   String state,
                                   String provider,
                                   Map<String, String> params) throws IOException {
        StringBuilder frontendUrl = new StringBuilder();

        if ("bind".equals(state)) {
            frontendUrl.append(baseUrl).append("/member/social");
        } else {
            frontendUrl.append(baseUrl).append("/login");
        }

        frontendUrl.append("?provider=").append(provider);
        frontendUrl.append("&status=ready");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                frontendUrl.append("&")
                        .append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }

        response.sendRedirect(frontendUrl.toString());
    }
}