package com.supernovapos.finalproject.auth.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.model.dto.PermissionDto;
import com.supernovapos.finalproject.auth.model.dto.RolePermissionsRequest;
import com.supernovapos.finalproject.auth.model.dto.RolePermissionsResponse;
import com.supernovapos.finalproject.auth.model.dto.RolesWithPermissionsResponse;

public interface RbacAdminService {

	/**
	 * 查詢所有角色與其綁定的權限
	 */
	RolesWithPermissionsResponse getRolesWithPermissions();

	/**
	 * 查詢角色的所有權限
	 */
	List<PermissionDto> getPermissionsByRoleId(Integer roleId);

	/**
	 * 覆蓋角色的權限綁定
	 */
	RolePermissionsResponse replacePermissions(RolePermissionsRequest req);

}