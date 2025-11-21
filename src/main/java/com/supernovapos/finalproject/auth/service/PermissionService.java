package com.supernovapos.finalproject.auth.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.model.dto.PermissionCreateRequest;
import com.supernovapos.finalproject.auth.model.dto.PermissionResponse;
import com.supernovapos.finalproject.auth.model.dto.PermissionUpdateRequest;

public interface PermissionService {

	/**
	 * 查詢所有權限
	 */
	Page<PermissionResponse> getAllPermissions(Pageable pageable);

	/**
	 * 查詢單一權限
	 */
	PermissionResponse getPermissionById(Long id);

	/**
	 * 給角色綁定頁使用：查詢啟用中的權限（含分類）
	 */
	List<PermissionResponse> getActivePermissionsForBinding();

	/**
	 * 新增權限
	 */
	PermissionResponse createPermission(PermissionCreateRequest req);

	/**
	 * 更新權限
	 */
	PermissionResponse updatePermission(Long id, PermissionUpdateRequest req);

	/**
	 * 停/啟用權限
	 */
	PermissionResponse updatePermissionStatus(Long id, boolean isAvailable);

	/**
	 * 刪除權限
	 */
	void deletePermission(Long permissionId);

}