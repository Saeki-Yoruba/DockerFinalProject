package com.supernovapos.finalproject.analytics.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.analytics.model.dto.DailySalesRequest;
import com.supernovapos.finalproject.analytics.model.dto.DailySalesResponse;
import com.supernovapos.finalproject.analytics.service.DailySalesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics/daily")
@Tag(name = "每日營收報表", description = "每日營收報表 API")
public class DailySalesController {

	private final DailySalesService dailySalesService;

	@Operation(summary = "查詢每日營收報表", description = "支援日期區間、月份、年份等模式查詢，並可分頁/排序")
	@PostMapping("/search")
	public ResponseEntity<Page<DailySalesResponse>> searchDailySales(
			@RequestBody DailySalesRequest request) {
		Page<DailySalesResponse> report = dailySalesService.getDailySalesReport(request);
		return ResponseEntity.ok(report);
	}

	@Operation(summary = "查詢單日營收 KPI", description = "傳入 yyyy-MM-dd，回傳當日的營收統計 (營收、訂單數、平均訂單金額)")
	@GetMapping("/{date}")
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public ResponseEntity<DailySalesResponse> getSingleDayReport(
			@PathVariable String date) {
		DailySalesRequest request = new DailySalesRequest();
		request.setMode("day");
		request.setDate(date);

		Page<DailySalesResponse> report = dailySalesService.getDailySalesReport(request);
		return report.hasContent()
				? ResponseEntity.ok(report.getContent().get(0))
				: ResponseEntity.notFound().build();
	}

}
