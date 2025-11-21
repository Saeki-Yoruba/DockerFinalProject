package com.supernovapos.finalproject.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.auth.model.dto.AuthRequest;
import com.supernovapos.finalproject.auth.model.dto.AuthResponse;
import com.supernovapos.finalproject.auth.service.AuthService;
import com.supernovapos.finalproject.auth.service.impl.AuthServiceImpl;
import com.supernovapos.finalproject.common.model.AuthErrorResponse;
import com.supernovapos.finalproject.common.model.ErrorResponse;
import com.supernovapos.finalproject.oauth.dto.GoogleTokenRequest;
import com.supernovapos.finalproject.oauth.dto.LineTokenRequest;
import com.supernovapos.finalproject.user.model.dto.UserRegisterDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.service.UserService;
import com.supernovapos.finalproject.user.service.impl.UserServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "註冊 / 登入 / 登出 / 第三方登入 API")
public class AuthController {

	private final UserService userService;
	private final AuthService authService;

	@Operation(summary = "使用者註冊", description = "建立帳號並寄送驗證信。")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "註冊成功，驗證信已寄出", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "409", description = "註冊失敗，Email 或手機已存在", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "系統錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegisterDto registerDto) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(userService.registerUser(registerDto));
	}

	@Operation(summary = "重新寄送驗證信", description = "當驗證信過期或遺失時，重新寄送驗證信。")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "驗證信已重新寄出"),
			@ApiResponse(responseCode = "409", description = "帳號不存在或已驗證", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PermitAll
	@PostMapping("/resend-verification")
	public ResponseEntity<Void> resendVerification(@RequestParam String username) {
		userService.resendVerificationEmail(username);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "帳號驗證", description = "使用 Email 連結中的驗證 token，啟用帳號。")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "驗證成功"),
			@ApiResponse(responseCode = "400", description = "驗證失敗", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
	})
	@GetMapping("/verify")
	@PermitAll
	public ResponseEntity<String> verifyAccount(@RequestParam String token) {
		userService.verifyAccount(token);
		return ResponseEntity.ok("帳號驗證成功，現在可以登入囉！");
	}

	@Operation(summary = "使用者登入", description = "以 Email 或手機號碼 + 密碼登入，成功後取得 JWT Token。")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "登入成功", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "401", description = "帳號或密碼錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "帳號未驗證 / 已停用", content = @Content(schema = @Schema(implementation = AuthErrorResponse.class)))
	})
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Google 快速登入", description = "使用 Google IdToken 登入系統")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登入成功", 
                         content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Google Token 無效 / 尚未綁定", 
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
	@PostMapping("/google-login")
	public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleTokenRequest req) {
	    return ResponseEntity.ok(authService.loginWithGoogle(req.getIdToken()));
	}
	
	@Operation(summary = "Line 快速登入", description = "使用 Line AccessToken 登入系統")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登入成功", 
                         content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Google Token 無效 / 尚未綁定", 
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
	@PostMapping("/line-login")
	public ResponseEntity<AuthResponse> lineLogin(@Valid @RequestBody LineTokenRequest req) {
	    return ResponseEntity.ok(authService.loginWithLine(req.getAccessToken()));
	}
	
	@Operation(summary = "使用者登出", description = "登出系統，前端需刪除保存的 JWT Token。")
	@ApiResponse(responseCode = "200", description = "登出成功")
	@PostMapping("/logout")
	@PermitAll
	public ResponseEntity<String> logout() {
		return ResponseEntity.ok("登出成功");
	}
}
