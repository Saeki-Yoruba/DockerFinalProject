package com.supernovapos.finalproject.analytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.analytics.model.dto.ProductImageResponse;
import com.supernovapos.finalproject.analytics.model.dto.ProductSaleRequest;
import com.supernovapos.finalproject.analytics.model.dto.ProductSaleResponse;
import com.supernovapos.finalproject.analytics.service.ProductSalesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics/product")
@Tag(name = "商品銷售分析", description = "提供商品銷售數據查詢與報表分析 API")
public class ProductSalesController {

	private final ProductSalesService productSalesService;

	@Operation(summary = "取得所有商品銷售報表", description = "回傳所有商品的銷售數量與營收統計（不含篩選條件，完整清單）", responses = {
			@ApiResponse(responseCode = "200", description = "成功取得報表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductSaleResponse.class)))),
			@ApiResponse(responseCode = "401", description = "未授權，請先登入"),
			@ApiResponse(responseCode = "403", description = "權限不足")
	})
	@GetMapping("/all")
	@PreAuthorize("hasAuthority('ANALYTICS_READ')")
	public ResponseEntity<List<ProductSaleResponse>> getAllProductsSale() {
		return ResponseEntity.ok(productSalesService.getAllProductReport());
	}

	@Operation(summary = "查詢商品銷售報表", description = "可依商品分類、營收範圍、銷售數量範圍進行篩選，並支援排序與 Top N 統計。", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "商品銷售篩選條件請求物件", content = @Content(schema = @Schema(implementation = ProductSaleRequest.class))), responses = {
			@ApiResponse(responseCode = "200", description = "成功取得查詢結果", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductSaleResponse.class)))),
			@ApiResponse(responseCode = "400", description = "請求參數錯誤"),
			@ApiResponse(responseCode = "401", description = "未授權，請先登入"),
			@ApiResponse(responseCode = "403", description = "權限不足")
	})
	@PostMapping("/search")
	@PreAuthorize("hasAuthority('ANALYTICS_SEARCH')")
	public ResponseEntity<List<ProductSaleResponse>> searchProducts(
			@RequestBody ProductSaleRequest request) {
		return ResponseEntity.ok(productSalesService.searchProductReport(request));
	}

	@Operation(summary = "取得人氣前三名商品圖片", description = "回傳銷售量最高的前 3 個商品及其圖片，供首頁展示使用。", responses = {
			@ApiResponse(responseCode = "200", description = "成功取得前三名商品圖片", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductImageResponse.class)))),
			@ApiResponse(responseCode = "401", description = "未授權，請先登入"),
			@ApiResponse(responseCode = "403", description = "權限不足")
	})
	@GetMapping("/top3")
	@PermitAll
	public ResponseEntity<List<ProductImageResponse>> getTop3PopularProducts() {
		return ResponseEntity.ok(productSalesService.getTop3PopularProducts());
	}
}
