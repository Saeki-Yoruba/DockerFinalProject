package com.supernovapos.finalproject.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.auth.service.AuthService;
import com.supernovapos.finalproject.common.model.ErrorResponse;
import com.supernovapos.finalproject.oauth.dto.GoogleTokenRequest;
import com.supernovapos.finalproject.oauth.dto.LineTokenRequest;
import com.supernovapos.finalproject.user.model.dto.BindResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserOrderResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserUpdateDto;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.service.UserBindingService;
import com.supernovapos.finalproject.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "User Self Service", description = "一般使用者自助操作 API")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserSelfController {

	private final UserService userService;
	private final AuthService authService;
	private final UserBindingService userBindingService;

	@Operation(summary = "取得目前登入者的資料", description = "根據 JWT 取得登入使用者的詳細資料。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "取得成功", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到用戶", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "系統錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponseDto> getCurrentUser() {
		User user = authService.getCurrentUser();
		return ResponseEntity.ok(userService.getCurrentUser(user));
	}

	@GetMapping("/orders")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "查詢我的訂單", description = "取得目前登入使用者的所有訂單", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Page<UserOrderResponseDto>> getMyOrders(
			@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		User user = authService.getCurrentUser();
		return ResponseEntity.ok(userService.getMyOrders(user, pageable));
	}

	@Operation(summary = "更新使用者資料", description = "允許修改密碼、暱稱、頭像與發票載具。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "資料驗證錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到用戶", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "系統錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PutMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponseDto> updateCurrentUser(@Valid @RequestBody UserUpdateDto dto) {
		User currentUser = authService.getCurrentUser();
		UserResponseDto updated = userService.updateUser(currentUser.getId(), dto);
		return ResponseEntity.ok(updated);
	}

	@Operation(summary = "停用帳號", description = """
			將目前登入者的帳號停用 (isActive = false)，資料保留但無法再登入。<br>
			⚠️ 注意：ROLE_ADMIN 不允許自我停用，以確保系統始終有管理員存在。
			""", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "停用成功"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足 / 管理員禁止自我停用", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到用戶", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "系統錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@DeleteMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> deactivateCurrentUser() {
		User currentUser = authService.getCurrentUser();
		userService.deactivateUser(currentUser.getId());
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "綁定 Google 帳號", description = "將目前登入者的帳號綁定至 Google UID。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "綁定成功"),
			@ApiResponse(responseCode = "400", description = "Google Token 無效 / 已被其他帳號綁定", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/bind-google")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BindResponseDto> bindGoogle(@RequestBody GoogleTokenRequest req) {
		return ResponseEntity.ok(userBindingService.bindGoogle(req.getIdToken()));
	}

	@Operation(summary = "解除綁定 Google 帳號", description = "將目前登入者的帳號解除與 Google 的綁定。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "解除綁定成功"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@DeleteMapping("/unbind-google")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BindResponseDto> unbindGoogle() {
		return ResponseEntity.ok(userBindingService.unbindGoogle());
	}

	@Operation(summary = "綁定 LINE 帳號", description = "將目前登入者的帳號綁定至 LINE UID。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "綁定成功"),
			@ApiResponse(responseCode = "400", description = "LINE Token 無效 / 已被其他帳號綁定", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/bind-line")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BindResponseDto> bindLine(@RequestBody LineTokenRequest req) {
		return ResponseEntity.ok(userBindingService.bindLine(req.getAccessToken()));
	}

	@Operation(summary = "解除綁定 LINE 帳號", description = "將目前登入者的帳號解除與 LINE 的綁定。", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "解除綁定成功"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@DeleteMapping("/unbind-line")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BindResponseDto> unbindLine() {
		return ResponseEntity.ok(userBindingService.unbindLine());
	}

}
