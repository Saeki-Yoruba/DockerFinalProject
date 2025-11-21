package com.supernovapos.finalproject.analytics.controller;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.analytics.model.dto.GroupOrderReportDto;
import com.supernovapos.finalproject.analytics.model.dto.RevenueTrendResponse;
import com.supernovapos.finalproject.analytics.service.GroupOrderReportService;
import com.supernovapos.finalproject.analytics.service.TrendService;
import com.supernovapos.finalproject.common.model.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics/group-orders")
@Tag(name = "群組訂單分析", description = "提供群組訂單的日/月/年統計分析 API")
public class GroupOrderReportController {

	private final GroupOrderReportService reportService;
	private final TrendService trendService;

	@Operation(summary = "查詢群組訂單報表", description = """
			可查詢群組訂單的統計資訊：
			- 如果傳入 `date`（格式 yyyy-MM-dd），則查單日報表。
			- 如果傳入 `month`（格式 yyyy-MM），則查單月報表。
			- 如果傳入 `year`（格式 yyyy），則查年度報表。
			- 三者都不傳時，預設查「昨天」。
			回傳包含 KPI、時段分布、桌位排行。
			""", responses = {
			@ApiResponse(responseCode = "200", description = "成功回傳報表資料", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupOrderReportDto.class), examples = {
					@ExampleObject(name = "成功範例", value = """
							{
							  "kpi": {
							    "groupCount": 32,
							    "totalRevenue": 56000,
							    "avgRevenue": 1750.5,
							    "avgDuration": 82.5
							  },
							  "periodDistribution": {
							    "午餐": 12,
							    "晚餐": 20,
							    "其他": 3
							  },
							  "topTables": [
							    { "tableId": 3, "revenue": 12000 },
							    { "tableId": 1, "revenue": 9500 },
							    { "tableId": 4, "revenue": 8700 }
							  ]
							}
							""")
			})),
			@ApiResponse(responseCode = "400", description = "參數格式錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "伺服器錯誤", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public GroupOrderReportDto getReport(
			@Parameter(description = "指定日期，格式 yyyy-MM-dd，例如 2025-09-21") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

			@Parameter(description = "指定月份，格式 yyyy-MM，例如 2025-09") @RequestParam(required = false) String month,

			@Parameter(description = "指定年份，格式 yyyy，例如 2025") @RequestParam(required = false) Integer year) {

		YearMonth ym = (month != null) ? YearMonth.parse(month) : null;
		return reportService.getReport(date, ym, year);
	}

	@Operation(summary = "取得營收趨勢", description = """
			依照模式(mode)與基準(base)查詢營收趨勢。
			- mode = date → 以「天」為單位，回傳近 7 天資料，base 格式：yyyy-MM-dd
			- mode = month → 以「月」為單位，回傳近 6 個月資料，base 格式：yyyy-MM
			- mode = year → 以「年」為單位，回傳近 5 年資料，base 格式：yyyy
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "成功取得營收趨勢", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RevenueTrendResponse.class))),
			@ApiResponse(responseCode = "400", description = "參數錯誤"),
			@ApiResponse(responseCode = "500", description = "伺服器錯誤")
	})
	@GetMapping("/revenue-trend")
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public RevenueTrendResponse getRevenueTrend(
			@Parameter(description = "模式，可選值：date / month / year", required = true, example = "date") @RequestParam String mode,
			@Parameter(description = "基準日期 (依 mode 帶不同格式)", required = true, example = "2025-09-18") @RequestParam String base) {
		return trendService.getRevenueTrend(mode, base);
	}

}
