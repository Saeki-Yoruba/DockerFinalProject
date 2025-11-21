package com.supernovapos.finalproject.auth.service;

import java.util.List;

import com.supernovapos.finalproject.auth.model.dto.RoleCreateRequest;
import com.supernovapos.finalproject.auth.model.dto.RoleResponse;
import com.supernovapos.finalproject.auth.model.dto.RoleUpdateRequest;

public interface RoleService {

	/**
	 * 查詢所有角色（不含權限細節）
	 */
	List<RoleResponse> getAllRoles();

	/**
	 * ADMIN 賦予角色 (只看啟用角色，排除 ADMIN 類)
	 */
	List<RoleResponse> getActiveRolesForAdmin();

	/**
	 * 店家賦予員工角色 (限定 STORE 類別 & 啟用)
	 */
	List<RoleResponse> getActiveStoreRoles();

	/**
	 * 依 ID 查角色
	 */
	RoleResponse getRoleById(Integer id);

	/**
	 * 依 Code 查角色
	 */
	RoleResponse getRoleByCode(String code);

	/**
	 * 建立新角色
	 * 僅允許建立 STORE 類角色，禁止新增 ADMIN/USER/OWNER
	 */
	RoleResponse createRole(RoleCreateRequest req);

	/**
	 * 更新角色名稱與分類
	 * 系統保留角色只能改 name，不能改 category
	 */
	RoleResponse updateRole(Integer id, RoleUpdateRequest req);

	/**
	 * 停/啟用角色
	 */
	RoleResponse updateRoleStatus(Integer id, boolean isAvailable);

	/**
	 * 刪除角色
	 */
	void deleteRole(Integer roleId);

}