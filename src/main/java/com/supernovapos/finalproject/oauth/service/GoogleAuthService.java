package com.supernovapos.finalproject.oauth.service;

import com.supernovapos.finalproject.oauth.dto.GoogleUserDto;

public interface GoogleAuthService {

	/**
	 * 建立 Google OAuth 登入 URL
	 */
	String buildAuthUrl(String mode);

	/**
	 * 用授權碼換取 Google access_token & id_token
	 */
	GoogleUserDto handleCallback(String code);

}