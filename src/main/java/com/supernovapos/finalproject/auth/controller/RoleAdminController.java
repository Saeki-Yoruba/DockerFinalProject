package com.supernovapos.finalproject.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.auth.model.dto.RoleCreateRequest;
import com.supernovapos.finalproject.auth.model.dto.RoleResponse;
import com.supernovapos.finalproject.auth.model.dto.RoleUpdateRequest;
import com.supernovapos.finalproject.auth.service.RoleService;
import com.supernovapos.finalproject.auth.service.impl.RoleServiceImpl;
import com.supernovapos.finalproject.common.model.ErrorResponse;
import com.supernovapos.finalproject.user.model.dto.StatusRequest;

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

@Slf4j
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色 CRUD API (限 ADMIN 使用)")
public class RoleAdminController {

	private final RoleService roleService;

	@Operation(summary = "查詢所有角色（含停用）", description = "取得系統中所有角色清單，包含停用角色（提供角色管理頁使用）。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoleResponse.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/all")
	@PreAuthorize("hasRole('ADMIN')")
	public List<RoleResponse> getAllRoles() {
		return roleService.getAllRoles();
	}

	@Operation(summary = "查詢啟用中角色（系統用）", description = "取得目前啟用中的角色清單，供 ADMIN 在後台分配角色使用。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoleResponse.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/active")
	@PreAuthorize("hasRole('ADMIN')")
	public List<RoleResponse> getActiveRolesForAdmin() {
		return roleService.getActiveRolesForAdmin();
	}

	@Operation(summary = "查詢啟用中角色（店家用）", description = "取得目前啟用中的店家角色清單（RoleCategoryEnum = STORE），供店主分配員工角色使用。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoleResponse.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/active/store")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OWNER')")
	public List<RoleResponse> getActiveStoreRoles() {
		return roleService.getActiveStoreRoles();
	}

	@Operation(summary = "查詢單一角色", description = "依 ID 查詢角色詳細資訊。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(schema = @Schema(implementation = RoleResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "角色不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public RoleResponse getRoleById(@PathVariable Integer id) {
		return roleService.getRoleById(id);
	}

	@Operation(summary = "建立新角色", description = "建立一個新的角色。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "建立成功", content = @Content(schema = @Schema(implementation = RoleResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "角色已存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RoleResponse> createRole(@RequestBody RoleCreateRequest req) {
		RoleResponse role = roleService.createRole(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(role);
	}

	@Operation(summary = "更新角色", description = "更新角色的名稱與分類。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = RoleResponse.class))),
			@ApiResponse(responseCode = "400", description = "不可修改", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "角色不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public RoleResponse updateRole(@PathVariable Integer id, @RequestBody RoleUpdateRequest req) {
		return roleService.updateRole(id, req);
	}

	@Operation(summary = "停/啟用角色", description = "停/啟用角色。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = RoleResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "角色不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RoleResponse> updateRoleStatus(
			@PathVariable Integer id,
			@RequestBody StatusRequest request) {

		RoleResponse updated = roleService.updateRoleStatus(id, request.isAvailable());
		return ResponseEntity.ok(updated);
	}

	@Operation(summary = "刪除角色", description = "刪除指定的角色。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "刪除成功"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(ref = "NotFoundErrorResponse")
	})
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
		roleService.deleteRole(id);
		return ResponseEntity.noContent().build();
	}
}
