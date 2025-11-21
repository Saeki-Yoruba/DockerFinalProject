package com.supernovapos.finalproject.product.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.product.dto.CreateCategoryRequest;
import com.supernovapos.finalproject.product.dto.UpdateCategoryRequest;
import com.supernovapos.finalproject.product.model.ProductCategory;
import com.supernovapos.finalproject.product.service.CategoryManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 管理端分類管理 API - 分類CRUD和狀態管理
 */
@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "管理端分類管理", description = "商品分類增刪修改和狀態管理功能")
@PreAuthorize("hasAnyRole('ADMIN','OWNER')")
public class AdminCategoryController {

	@Autowired
	private CategoryManagementService categoryManagementService;


//===== 分類基本CRUD操作 =====

//	新增商品分類

	@Operation(summary = "新增商品分類", description = "新增一個新的商品分類")
	@PostMapping
	public ResponseEntity<Map<String, Object>> createCategory(@RequestBody CreateCategoryRequest request) {

		ProductCategory category = categoryManagementService.createCategory(request);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "商品分類新增成功");
		response.put("category", category);

		return ResponseEntity.ok(response);
	}

//	查詢分類(取得所有分類)
	@Operation(summary = "取得所有分類", description = "管理端查看所有分類，包含已停用的分類")
	@GetMapping
	public ResponseEntity<List<ProductCategory>> getAllCategories() {
		List<ProductCategory> categories = categoryManagementService.getAllCategory();
		return ResponseEntity.ok(categories);
	}

//	更新商品分類資訊
	@Operation(summary = "更新分類資訊", description = "更新商品分類的基本資訊")
	@PutMapping("/{categoryId}")
	public ResponseEntity<Map<String, Object>> updateCategory(
			@Parameter(description = "分類ID") @PathVariable Integer categoryId,
			@RequestBody UpdateCategoryRequest request) {

		ProductCategory category = categoryManagementService.updateCategory(categoryId, request);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "分類資訊更新成功");
		response.put("category", category);

		return ResponseEntity.ok(response);
	}

//	刪除分類
	@Operation(summary = "刪除分類", description = "刪除商品分類（需要分類內無商品）")
	@DeleteMapping("/{categoryId}")
	public ResponseEntity<Map<String, String>> deleteCategory(
			@Parameter(description = "分類ID") @PathVariable Integer categoryId) {

		categoryManagementService.deleteCategory(categoryId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "分類已刪除");

		return ResponseEntity.ok(response);
	}

//===== 分類狀態管理 =====

//	啟用分類
	@Operation(summary = "啟用分類", description = "啟用商品分類，使其在客戶端顯示")
	@PutMapping("/{categoryId}/enable")
	public ResponseEntity<Map<String, String>> enableCategory(
			@Parameter(description = "分類ID") @PathVariable Integer categoryId) {

		categoryManagementService.enableCategory(categoryId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "分類成功啟用");

		return ResponseEntity.ok(response);
	}

//	停用分類
	@Operation(summary = "停用分類", description = "停用商品分類，同時停用分類底下的商品")
	@PutMapping("/{categoryId}/disable")
	public ResponseEntity<Map<String, String>> disableCategory(
			@Parameter(description = "分類ID") @PathVariable Integer categoryId) {

		categoryManagementService.disableCategory(categoryId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "分類已停用");

		return ResponseEntity.ok(response);
	}

}
