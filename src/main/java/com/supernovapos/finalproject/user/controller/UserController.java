package com.supernovapos.finalproject.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.common.model.ErrorResponse;
import com.supernovapos.finalproject.user.model.dto.StatusRequest;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserUpdateDto;
import com.supernovapos.finalproject.user.service.UserService;
import com.supernovapos.finalproject.user.service.impl.UserServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "使用者管理相關 API")
public class UserController {

	private final UserService userService;

	@Operation(summary = "依照 ID 查詢使用者", description = "根據使用者的唯一 ID 取得完整使用者資料。")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功取得使用者資訊", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "404", description = "找不到用戶", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/{id}")
	@PermitAll
	public ResponseEntity<UserResponseDto> findUserById(@PathVariable Long id) {
		return userService.findUserById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Operation(summary = "取得所有使用者清單", description = "需要 ADMIN 權限", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功取得使用者清單", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class)))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponseDto>> findAllUsers(Pageable pageable) {
		return ResponseEntity.ok(userService.findAllUsers(pageable));
	}

	@Operation(summary = "更新使用者資料（ADMIN 使用）", description = "允許 ADMIN 修改使用者密碼、暱稱、頭像與發票載具。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "資料驗證錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到用戶", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "用戶已存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponseDto> updateUser(
			@PathVariable Long id,
			@Valid @RequestBody UserUpdateDto dto) {
		return ResponseEntity.ok(userService.updateUser(id, dto));
	}

	@Operation(
		    summary = "更新使用者帳號狀態",
		    description = "根據指定 ID 停/啟用使用者帳號 (管理員不可停用自己)",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@ApiResponses({
		    @ApiResponse(
		        responseCode = "200",
		        description = "狀態更新成功",
		        content = @Content(schema = @Schema(implementation = UserResponseDto.class))
		    ),
		    @ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		    @ApiResponse(responseCode = "404", description = "找不到用戶", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		    @ApiResponse(responseCode = "409", description = "用戶已存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		})
	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponseDto> updateUserStatus(
	        @PathVariable Long id,
	        @RequestBody StatusRequest request) {
	    return ResponseEntity.ok(userService.updateUserStatus(id, request.isAvailable()));
	}
}
