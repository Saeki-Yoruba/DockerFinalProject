package com.supernovapos.finalproject.store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.common.model.ErrorResponse;
import com.supernovapos.finalproject.store.model.StoreAdminResponseDto;
import com.supernovapos.finalproject.store.model.StoreResponseDto;
import com.supernovapos.finalproject.store.model.StoreStatusResponse;
import com.supernovapos.finalproject.store.model.StoreUpdateDto;
import com.supernovapos.finalproject.store.service.StoreService;
import com.supernovapos.finalproject.store.service.impl.StoreServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@Tag(name = "Store", description = "APIs for getting and updating store information")
public class StoreController {

	private final StoreService storeService;

	@Operation(summary = "取得商店資訊 (前台)", description = "提供前台顧客使用，回傳店家基本資訊 (名稱、電話、地址、logo、banner...)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功取得商店資訊", content = @Content(schema = @Schema(implementation = StoreResponseDto.class))),
			@ApiResponse(responseCode = "404", description = "找不到商店資訊")
	})
	@GetMapping
	@PermitAll
	public ResponseEntity<StoreResponseDto> getStore() {
		return ResponseEntity.ok(storeService.getStoreForCustomer());
	}

	@Operation(summary = "檢查商店是否啟用", description = "回傳商店是否啟用 (isActive)，提供前端頁面守衛判斷用。")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "檢查成功", content = @Content(schema = @Schema(implementation = StoreStatusResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到商店資訊")
	})
	@GetMapping("/status")
	@PermitAll
	public ResponseEntity<StoreStatusResponse> getStoreStatus() {
		boolean isActive = storeService.isStoreActive();
		return ResponseEntity.ok(new StoreStatusResponse(isActive));
	}

	@Operation(summary = "取得商店資訊（Admin）", description = "管理員 / 店長存取，回傳完整的商店設定資訊", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功取得商店資訊", content = @Content(schema = @Schema(implementation = StoreAdminResponseDto.class))),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到商店資訊")
	})
	@GetMapping("/manager")
	@PreAuthorize("hasAuthority('STORE_READ')")
	public ResponseEntity<StoreAdminResponseDto> getAdminStore() {
		return ResponseEntity.ok(storeService.getAdminStore());
	}

	@Operation(summary = "更新商店資訊", description = "ADMIN 可修改所有欄位，OWNER 不能修改 isActive", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = StoreAdminResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "請求格式錯誤"),
			@ApiResponse(responseCode = "401", description = "未登入 / JWT 無效", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "權限不足", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "找不到商店資訊")
	})
	@PutMapping
	@PreAuthorize("hasAuthority('STORE_UPDATE')")
	public ResponseEntity<StoreAdminResponseDto> updateStore(
			@RequestBody StoreUpdateDto dto,
			Authentication authentication) {
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		StoreAdminResponseDto updated = storeService.updateStore(dto, isAdmin);
		return ResponseEntity.ok(updated);
	}
}
