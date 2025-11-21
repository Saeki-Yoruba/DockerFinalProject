package com.supernovapos.finalproject.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.auth.model.dto.PermissionDto;
import com.supernovapos.finalproject.auth.model.dto.RolePermissionsRequest;
import com.supernovapos.finalproject.auth.model.dto.RolePermissionsResponse;
import com.supernovapos.finalproject.auth.model.dto.RolesWithPermissionsResponse;
import com.supernovapos.finalproject.auth.service.RbacAdminService;
import com.supernovapos.finalproject.auth.service.impl.RbacAdminServiceImpl;
import com.supernovapos.finalproject.common.model.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/rbac")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RBAC 管理", description = "角色與權限綁定 API (限 ADMIN 使用)")
public class RbacAdminController {

	private final RbacAdminService rbacAdminService;

	@Operation(summary = "覆蓋角色權限綁定（REPLACE）", description = "清空角色原有綁定後，重新建立新的權限綁定。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "綁定成功", content = @Content(schema = @Schema(implementation = RolePermissionsResponse.class))),
			@ApiResponse(responseCode = "400", description = "權限已被停用", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限 / 角色不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PutMapping("/permissions")
	@PreAuthorize("hasRole('ADMIN')")
	public RolePermissionsResponse replace(@RequestBody RolePermissionsRequest req) {
		log.info("REPLACE 請求: {}", req);
		return rbacAdminService.replacePermissions(req);
	}

	@Operation(summary = "查詢所有角色與其綁定權限", description = "回傳系統中所有角色及其目前綁定的權限清單。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/roles-permissions")
	@PreAuthorize("hasRole('ADMIN')")
	public RolesWithPermissionsResponse getRolesWithPermissions() {
		return rbacAdminService.getRolesWithPermissions();
	}

	@Operation(summary = "查詢角色權限", description = "取得指定角色目前擁有的權限", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功回傳權限清單", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PermissionDto.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限 / 角色不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/roles/{id}/permissions")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<PermissionDto>> getPermissionsByRole(@PathVariable Integer id) {
		return ResponseEntity.ok(rbacAdminService.getPermissionsByRoleId(id));
	}
}
