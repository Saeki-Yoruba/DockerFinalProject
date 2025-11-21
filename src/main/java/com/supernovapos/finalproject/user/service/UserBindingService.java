package com.supernovapos.finalproject.user.service;

import java.util.Map;

import com.supernovapos.finalproject.user.model.dto.BindResponseDto;

public interface UserBindingService {

	BindResponseDto bindGoogle(String idToken);

	BindResponseDto unbindGoogle();

	BindResponseDto bindLine(String accessToken);

	BindResponseDto unbindLine();

}