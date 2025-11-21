package com.supernovapos.finalproject.oauth.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.common.util.OAuthRedirectUtil;
import com.supernovapos.finalproject.oauth.dto.LineUserDto;
import com.supernovapos.finalproject.oauth.service.LineAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "Line OAuth", description = "LINE 第三方登入流程 API")
@RestController
@RequestMapping("/api/oauth/line")
@RequiredArgsConstructor
public class LineAuthController {

    private final LineAuthService lineAuthService;
    private final OAuthRedirectUtil oAuthRedirectUtil;

    /**
     * 產生 LINE 登入 URL
     */
    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getLoginUrl(
            @RequestParam(defaultValue = "login") String mode) {
        String url = lineAuthService.buildAuthUrl(mode);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * LINE OAuth callback
     * - 拿 code 換 access_token / id_token
     * - 拉取 user profile
     * - redirect 回前端
     */
    @GetMapping("/callback")
    public void lineCallback(@RequestParam String code,
                             @RequestParam(required = false, defaultValue = "login") String state,
                             HttpServletResponse response) throws IOException {
        LineUserDto result = lineAuthService.handleCallback(code);

        oAuthRedirectUtil.redirectToFrontend(response, state, "line", Map.of(
                "accessToken", result.getAccessToken(),
                "lineUid", result.getLineUid(),
                "name", result.getName()
        ));
    }

}
