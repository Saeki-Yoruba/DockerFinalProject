package com.supernovapos.finalproject.analytics.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.analytics.model.dto.MonthlySpendingDto;
import com.supernovapos.finalproject.analytics.model.dto.UserSpendingDto;
import com.supernovapos.finalproject.analytics.model.dto.UserSpendingRequest;
import com.supernovapos.finalproject.analytics.model.dto.UserSpendingSummaryDto;
import com.supernovapos.finalproject.analytics.service.UserSpendingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics/user-spending")
@Tag(name = "顧客消費分析", description = "提供顧客消費數據查詢與報表分析 API")
public class UserSpendingController {

	private final UserSpendingService service;

	@Operation(summary = "分頁查詢顧客消費報表", description = "回傳符合篩選條件的顧客消費報表（支援分頁、排序、關鍵字、金額篩選與日期區間）。", security = @SecurityRequirement(name = "bearerAuth"), responses = {
			@ApiResponse(responseCode = "200", description = "成功取得顧客消費報表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSpendingDto.class))),
			@ApiResponse(responseCode = "400", description = "請求參數錯誤"),
			@ApiResponse(responseCode = "401", description = "未授權，請先登入"),
			@ApiResponse(responseCode = "403", description = "權限不足")
	})
	@GetMapping
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public Page<UserSpendingDto> search(UserSpendingRequest req) {
		return service.search(req);
	}

	@Operation(summary = "顧客消費 KPI 總覽", description = "回傳符合篩選條件的 KPI 指標（總會員數、總消費金額、平均客單價、活躍會員數）。", security = @SecurityRequirement(name = "bearerAuth"), responses = {
			@ApiResponse(responseCode = "200", description = "成功取得 KPI 總覽", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSpendingSummaryDto.class))),
			@ApiResponse(responseCode = "401", description = "未授權，請先登入"),
			@ApiResponse(responseCode = "403", description = "權限不足")
	})
	@GetMapping("/summary")
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public UserSpendingSummaryDto summary(UserSpendingRequest req) {
		return service.getSummary(req);
	}

	@Operation(summary = "Top N 消費者排行", description = "依總消費金額排序，回傳前 N 名顧客的消費統計，預設 N=10。", security = @SecurityRequirement(name = "bearerAuth"), parameters = {
			@Parameter(name = "limit", description = "要查詢的前 N 名，預設為 10", example = "10")
	}, responses = {
			@ApiResponse(responseCode = "200", description = "成功取得排行", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserSpendingDto.class)))),
			@ApiResponse(responseCode = "401", description = "未授權，請先登入"),
			@ApiResponse(responseCode = "403", description = "權限不足")
	})
	@GetMapping("/top")
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public List<UserSpendingDto> top(@RequestParam(defaultValue = "10") int limit) {
		return service.findTopN(limit);
	}

	@GetMapping("/monthly")
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public List<MonthlySpendingDto> monthly(UserSpendingRequest req) {
		return service.getMonthlyTrend(req);
	}
}
