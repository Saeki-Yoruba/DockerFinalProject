package com.supernovapos.finalproject.user.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.user.model.dto.StaffRegisterDto;
import com.supernovapos.finalproject.user.model.dto.StaffRoleResponse;
import com.supernovapos.finalproject.user.model.dto.StaffUpdateDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;

public interface StaffService {

	/**
	 * 建立員工
	 */
	UserResponseDto createStaff(StaffRegisterDto dto);

	/**
	 * 查詢所有員工 + 篩選
	 */
	Page<UserResponseDto> findAllStaff(Pageable pageable, String role);

	/**
	 * 員工角色賦予用查詢
	 */
	List<StaffRoleResponse> getStaffRoles(Long userId);

	/**
	 * 修改員工
	 */
	UserResponseDto updateStaff(Long id, StaffUpdateDto dto);

	/**
	 * 員工離職
	 */
	UserResponseDto deactivateStaff(Long id);

}