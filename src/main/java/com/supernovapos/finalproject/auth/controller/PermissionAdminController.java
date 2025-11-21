package com.supernovapos.finalproject.auth.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.auth.model.dto.PermissionCreateRequest;
import com.supernovapos.finalproject.auth.model.dto.PermissionResponse;
import com.supernovapos.finalproject.auth.model.dto.PermissionUpdateRequest;
import com.supernovapos.finalproject.auth.service.PermissionService;
import com.supernovapos.finalproject.auth.service.impl.PermissionServiceImpl;
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
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "權限管理", description = "權限 CRUD API (限 ADMIN 使用)")
public class PermissionAdminController {

	private final PermissionService permissionService;

	@Operation(summary = "查詢所有權限", description = "回傳系統中所有權限清單。", security = @SecurityRequirement(name = "bearerAuth"), responses = {
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<PermissionResponse>> getAllPermissions(Pageable pageable) {
	    return ResponseEntity.ok(permissionService.getAllPermissions(pageable));
	}

	@Operation(summary = "查詢啟用中的權限", description = "提供角色綁定頁使用，僅回傳啟用中的權限清單。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PermissionResponse.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/active")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<PermissionResponse>> getActivePermissionsForBinding() {
		return ResponseEntity.ok(permissionService.getActivePermissionsForBinding());
	}

	@Operation(summary = "查詢單一權限", description = "依 ID 查詢權限詳細資訊。", security = @SecurityRequirement(name = "bearerAuth"), responses = {
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public PermissionResponse getPermissionById(@PathVariable Long id) {
		return permissionService.getPermissionById(id);
	}

	@Operation(summary = "新增權限", description = "建立一個新的權限，需指定分類、代碼、HTTP 方法與 URL。", security = @SecurityRequirement(name = "bearerAuth"), requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "新增權限請求內容", required = true, content = @Content(schema = @Schema(implementation = PermissionCreateRequest.class))), responses = {
			@ApiResponse(responseCode = "201", description = "建立成功", content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
			@ApiResponse(ref = "BadRequestResponse"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "權限CODE已存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PermissionResponse> createPermission(@RequestBody PermissionCreateRequest req) {
		PermissionResponse saved = permissionService.createPermission(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@Operation(summary = "更新權限", description = "更新指定 ID 的權限資訊。", security = @SecurityRequirement(name = "bearerAuth"), requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "更新權限請求內容", required = true, content = @Content(schema = @Schema(implementation = PermissionUpdateRequest.class))), responses = {
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
			@ApiResponse(ref = "BadRequestResponse"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(ref = "ConflictResponse")
	})
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public PermissionResponse updatePermission(@PathVariable Long id, @RequestBody PermissionUpdateRequest req) {
		return permissionService.updatePermission(id, req);
	}

	@Operation(summary = "更新權限狀態", description = "根據指定 ID 更新權限是否啟用 (isAvailable)。", security = @SecurityRequirement(name = "bearerAuth"), responses = {
			@ApiResponse(responseCode = "200", description = "狀態更新成功", content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PermissionResponse> updatePermissionStatus(
			@PathVariable Long id,
			@RequestBody StatusRequest request) {
		PermissionResponse updated = permissionService.updatePermissionStatus(id, request.isAvailable());
		return ResponseEntity.ok(updated);
	}

	@Operation(summary = "刪除權限", description = "刪除指定 ID 的權限。", security = @SecurityRequirement(name = "bearerAuth"), responses = {
			@ApiResponse(responseCode = "204", description = "刪除成功"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
		permissionService.deletePermission(id);
		return ResponseEntity.noContent().build();
	}
}
