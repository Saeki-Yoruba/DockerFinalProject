package com.supernovapos.finalproject.auth.service;

import com.supernovapos.finalproject.auth.model.dto.AuthRequest;
import com.supernovapos.finalproject.auth.model.dto.AuthResponse;
import com.supernovapos.finalproject.user.model.entity.User;

public interface AuthService {

	/**
	 * 取得目前登入的 User ID
	 */
	Long getCurrentUserId();

	/**
	 * 取得目前登入的完整 User 實體
	 */
	User getCurrentUser();

	/**
	 * 帳號密碼登入流程
	 */
	AuthResponse login(AuthRequest request);

	/**
	 * Google 快速登入
	 */
	AuthResponse loginWithGoogle(String idToken);

	/**
	 * LINE 快速登入
	 */
	AuthResponse loginWithLine(String accessToken);

}