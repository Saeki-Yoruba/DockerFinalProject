package com.supernovapos.finalproject.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.auth.model.dto.PermissionCategoryDto;
import com.supernovapos.finalproject.auth.service.PermissionCategoryService;
import com.supernovapos.finalproject.auth.service.impl.PermissionCategoryServiceImpl;
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

@RestController
@RequestMapping("/api/admin/permission-groups")
@RequiredArgsConstructor
@Tag(name = "Permission Groups", description = "權限模組管理 API")
public class PermissionGroupController {

	private final PermissionCategoryService groupService;

	@Operation(summary = "建立權限模組", description = "新增一個權限模組（例如：用戶管理、訂單管理）", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "建立成功", content = @Content(schema = @Schema(implementation = PermissionCategoryDto.class))),
			@ApiResponse(ref = "BadRequestResponse"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "權限群組已存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PermissionCategoryDto> create(@RequestBody PermissionCategoryDto req) {
		return ResponseEntity.ok(groupService.createCategory(req.getCategoryName(), req.getDescription()));
	}

	@Operation(summary = "查詢所有權限模組", description = "取得所有權限模組清單", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PermissionCategoryDto.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<PermissionCategoryDto>> findAll() {
		return ResponseEntity.ok(groupService.getAllCategories());
	}

	@Operation(summary = "查詢單一權限模組Response", description = "依 ID 查詢權限模組Response", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(schema = @Schema(implementation = PermissionCategoryDto.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限群組不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PermissionCategoryDto> findById(@PathVariable Integer id) {
		return ResponseEntity.ok(groupService.getCategory(id));
	}

	@Operation(summary = "更新權限模組", description = "更新指定 ID 的權限模組名稱或描述", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = PermissionCategoryDto.class))),
			@ApiResponse(responseCode = "400", description = "輸入格式錯誤 / 驗證失敗", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "權限群組不存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "權限群組已存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PermissionCategoryDto> update(
			@PathVariable Integer id,
			@RequestBody PermissionCategoryDto req) {
		return ResponseEntity.ok(groupService.updateCategory(id, req.getCategoryName(), req.getDescription()));
	}

	@Operation(summary = "刪除權限模組", description = "刪除指定 ID 的權限模組（底下不能有權限）", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "刪除成功"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		groupService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}
}
