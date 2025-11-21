package com.supernovapos.finalproject.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.product.model.ProductCategory;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;

/**
 * 客戶端商品查詢 API - 提供商品瀏覽和搜尋功能
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "客戶端商品查詢", description = "客戶端商品瀏覽和搜尋功能")
@PermitAll
public class ProductController {

	@Autowired
	private ProductService productService;

//	取得所有可用商品
	@Operation(summary = "取得所有可用商品", description = "取得所有上架中的商品列表")
	@GetMapping
	@PermitAll
	public ResponseEntity<List<Products>> getAllAvailableProducts() {

		List<Products> products = productService.getAllAvailableProducts();
		return ResponseEntity.ok(products);
	}

//	根據分類查詢可用商品
	@Operation(summary = "依分類查詢商品", description = "根據分類ID查詢該分類下的所有可用商品")
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<Products>> getProductsByCategory(
			@Parameter(description = "商品分類ID") @PathVariable Integer categoryId) {

		List<Products> products = productService.getAvailableProductsByCategory(categoryId);
		return ResponseEntity.ok(products);
	}

//	根據商品ID查詢商品詳情
	@Operation(summary = "查詢商品詳情", description = "根據商品ID取得商品詳細資訊")
	@GetMapping("/{productId}")
	public ResponseEntity<Products> getProductById(
			@Parameter(description = "商品ID") @PathVariable Integer productId) {

		Products product = productService.getProductById(productId);

		return ResponseEntity.ok(product);
	}

//	搜尋商品
	@Operation(summary = "搜尋商品", description = "根據關鍵字搜尋商品名稱")
	@GetMapping("/search")
	public ResponseEntity<List<Products>> searchProducts(
			@Parameter(description = "搜尋關鍵字") @RequestParam String keyword) {

		List<Products> products = productService.searchProducts(keyword);
		return ResponseEntity.ok(products);
	}

//	取得熱門商品
	@Operation(summary = "取得熱門商品", description = "取得銷量排行的熱門商品")
	@GetMapping("/popular")
	public ResponseEntity<List<Products>> getPopularProducts(
			@Parameter(description = "取得數量，預設10筆") @RequestParam(defaultValue = "10") Integer limit) {
		Pageable pageable = PageRequest.of(0, limit);
		List<Products> products = productService.getPopularProducts(pageable);

		return ResponseEntity.ok(products);
	}

//	分頁查詢商品
	@Operation(summary = "分頁查詢商品", description = "支援分類篩選的分頁查詢")
	@GetMapping("/page")
	public ResponseEntity<Page<Products>> getProductsWithPagination(
			@Parameter(description = "分類ID，可選") @RequestParam(required = false) Integer categoryId,
			@Parameter(description = "頁碼，從0開始") @RequestParam(defaultValue = "0") Integer page,
			@Parameter(description = "每頁數量") @RequestParam(defaultValue = "20") Integer size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Products> products = productService.getProductsWithPagination(categoryId, pageable);

		return ResponseEntity.ok(products);
	}

//	取得所有啟用的商品分類
	@Operation(summary = "取得有商品的分類", description = "取得有可用商品的分類列表")
	@GetMapping("/categories/with-products")
	public ResponseEntity<List<ProductCategory>> getCategoriesWithProducts() {
		List<ProductCategory> categories = productService.getCategoriesWithProducts();
		return ResponseEntity.ok(categories);
	}

//	根據分類ID查詢分類詳情
	@Operation(summary = "查詢分類詳情", description = "根據分類ID取得分類詳細資訊")
	@GetMapping("/categories/{categoryId}")
	public ResponseEntity<ProductCategory> getCategoryById(
			@Parameter(description = "分類ID") @PathVariable Integer categoryId) {
		ProductCategory category = productService.getcategoryById(categoryId);
		return ResponseEntity.ok(category);
	}

//	根據分類名稱查詢分類
	@Operation(summary = "依名稱查詢分類", description = "根據分類名稱查詢分類資訊")
	@GetMapping("/categories/by-name/{categoryName}")
	public ResponseEntity<ProductCategory> getCategoryByName(
			@Parameter(description = "分類名稱") @PathVariable String categoryName) {
		ProductCategory category = productService.getCategoryByName(categoryName);
		return ResponseEntity.ok(category);
	}

}
