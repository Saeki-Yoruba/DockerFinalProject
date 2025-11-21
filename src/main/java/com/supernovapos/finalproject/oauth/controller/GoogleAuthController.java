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
import com.supernovapos.finalproject.oauth.dto.GoogleUserDto;
import com.supernovapos.finalproject.oauth.service.GoogleAuthService;
import com.supernovapos.finalproject.user.service.UserBindingService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/oauth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

	private final GoogleAuthService googleAuthService;
	private final OAuthRedirectUtil oAuthRedirectUtil;

	/**
	 * 取得 Google OAuth 登入連結
	 */
	@GetMapping("/login-url")
	@PermitAll
	public ResponseEntity<Map<String, String>> getLoginUrl(@RequestParam(defaultValue = "login") String mode) {
		String url = googleAuthService.buildAuthUrl(mode);
		return ResponseEntity.ok(Map.of("url", url));
	}

	/**
	 * Google 回調，換取 user 資訊
	 */
	@GetMapping("/callback")
	public void googleCallback(@RequestParam String code,
	                           @RequestParam(required = false, defaultValue = "login") String state,
	                           HttpServletResponse response) throws IOException {
	    GoogleUserDto result = googleAuthService.handleCallback(code);

	    oAuthRedirectUtil.redirectToFrontend(response, state, "google", Map.of(
	            "idToken", result.getIdToken(),
	            "email", result.getEmail()
	    ));
	}

}