package com.supernovapos.finalproject.oauth.service;

import com.supernovapos.finalproject.oauth.dto.LineUserDto;

public interface LineAuthService {

	String buildAuthUrl(String mode);

	LineUserDto handleCallback(String code);

}