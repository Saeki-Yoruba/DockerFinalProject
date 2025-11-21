package com.supernovapos.finalproject.user.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.common.model.ErrorResponse;
import com.supernovapos.finalproject.user.model.dto.StaffRegisterDto;
import com.supernovapos.finalproject.user.model.dto.StaffRoleResponse;
import com.supernovapos.finalproject.user.model.dto.StaffUpdateDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.service.StaffService;
import com.supernovapos.finalproject.user.service.impl.StaffServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff")
@Tag(name = "Staff Management", description = "員工管理 API（ADMIN/OWNER 使用）")
public class StaffController {

	private final StaffService staffService;

	@Operation(summary = "新增員工", description = "建立新員工帳號，需提供 Email、密碼、暱稱、至少一個角色。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "建立成功", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "輸入驗證錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping
	@PreAuthorize("hasAuthority('STAFF_CREATE')")
	public ResponseEntity<UserResponseDto> createStaff(@Valid @RequestBody StaffRegisterDto dto) {
		UserResponseDto staff = staffService.createStaff(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(staff);
	}

	@Operation(summary = "更新員工資料", description = "修改指定員工的資料（密碼 / 暱稱 / 頭像 / 角色）。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "輸入格式錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到該員工", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('STAFF_UPDATE')")
	public ResponseEntity<UserResponseDto> updateStaff(
			@PathVariable Long id, @Valid @RequestBody StaffUpdateDto dto) {
		return ResponseEntity.ok(staffService.updateStaff(id, dto));
	}

	@Operation(summary = "查詢員工角色", description = "取得指定員工的角色清單（含已分配狀態），用於角色分配的 checkbox。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功取得角色清單", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StaffRoleResponse.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到員工", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/{id}/roles")
	@PreAuthorize("hasAuthority('STAFF_READ')")
	public ResponseEntity<List<StaffRoleResponse>> getStaffRoles(@PathVariable Long id) {
		return ResponseEntity.ok(staffService.getStaffRoles(id));
	}

	@Operation(summary = "查詢所有員工", description = "回傳所有員工帳號，支援分頁（僅限 ADMIN/OWNER 使用）。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功取得員工清單", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping
	@PreAuthorize("hasAuthority('STAFF_READ')")
	public ResponseEntity<Page<UserResponseDto>> findAllStaff(
			Pageable pageable,
			@RequestParam(required = false) String role) {
		return ResponseEntity.ok(staffService.findAllStaff(pageable, role));
	}

	@Operation(summary = "員工離職", description = "將指定員工帳號角色權限停用（移除 STORE 類別角色，但保留 OWNER；若無 USER 會自動補上）。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "停權成功", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到該員工", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PutMapping("/{id}/deactivate")
	@PreAuthorize("hasAuthority('STAFF_DELETE')")
	public ResponseEntity<UserResponseDto> deactivateStaff(@PathVariable Long id) {
		return ResponseEntity.ok(staffService.deactivateStaff(id));
	}
}
