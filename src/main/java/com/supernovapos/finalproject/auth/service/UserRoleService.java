package com.supernovapos.finalproject.auth.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.user.model.dto.UserResponseDto;

public interface UserRoleService {

	UserResponseDto replaceUserRoles(Long userId, List<String> roleCodes);

	List<String> getUserRoleCodes(Long userId);

}