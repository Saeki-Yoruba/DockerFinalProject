package com.supernovapos.finalproject.product.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.product.dto.BatchUpdateProductStatusRequest;
import com.supernovapos.finalproject.product.dto.CategoryProductStatsDto;
import com.supernovapos.finalproject.product.dto.CreateProductRequest;
import com.supernovapos.finalproject.product.dto.ProductStatsDto;
import com.supernovapos.finalproject.product.dto.UpdateProductRequest;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.service.ProductManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 管理端商品管理 API - 商品CRUD和狀態管理
 */
@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "管理端商品管理", description = "商品增刪修改和狀態管理功能")
@PreAuthorize("hasAnyRole('ADMIN','OWNER')")
public class AdminProductController {

	@Autowired
	private ProductManagementService productManagementService;

//===== 商品基本CRUD操作 =====

//	新增商品
	@Operation(summary = "新增商品", description = "新增一個商品到指定分類")
	@PostMapping
	public ResponseEntity<Map<String, Object>> createProduct(@RequestBody CreateProductRequest request) {
		Products product = productManagementService.createProduct(request);

		Map<String, Object> response = new HashMap<>();
		response.put("sucecss", true);
		response.put("message", "商品新增成功");
		response.put("product", product);

		return ResponseEntity.ok(response);
	}

//	更新商品
	@Operation(summary = "更新商品", description = "更新商品的基本資訊")
	@PutMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> updateProduct(
			@Parameter(description = "商品ID") @PathVariable Integer productId,
			@RequestBody UpdateProductRequest request) {

		Products product = productManagementService.updateProduct(productId, request);

		Map<String, Object> response = new HashMap<>();
		response.put("sucecss", true);
		response.put("message", "商品更新成功");
		response.put("product", product);

		return ResponseEntity.ok(response);
	}

//	刪除商品
	@Operation(summary = "刪除商品", description = "永久刪除商品（謹慎使用）")
	@DeleteMapping("/{productId}")
	public ResponseEntity<Map<String, String>> deleteProduct(
			@Parameter(description = "商品ID") @PathVariable Integer productId) {

		productManagementService.deleteProduct(productId);

		Map<String, String> response = new HashMap<>();
		response.put("sucecss", "true");
		response.put("message", "商品已刪除");

		return ResponseEntity.ok(response);
	}

//===== 商品狀態管理 =====

//	下架商品
	@Operation(summary = "下架商品", description = "將商品設為不可用（軟刪除）")
	@PutMapping("/{productId}/disable")
	public ResponseEntity<Map<String, String>> disableProduct(
			@Parameter(description = "商品ID") @PathVariable Integer productId) {

		productManagementService.disableProduct(productId);

		Map<String, String> response = new HashMap<>();
		response.put("sucecss", "true");
		response.put("message", "商品已下架");

		return ResponseEntity.ok(response);
	}

//	上架商品
	@Operation(summary = "上架商品", description = "將商品設為可用")
	@PutMapping("/{productId}/enable")
	public ResponseEntity<Map<String, String>> enableProduct(
			@Parameter(description = "商品ID") @PathVariable Integer productId) {
		
		productManagementService.enableProduct(productId);

		Map<String, String> response = new HashMap<>();
		response.put("sucecss", "true");
		response.put("message", "商品已上架");

		return ResponseEntity.ok(response);
	}
	
//	批量更新商品狀態
	@Operation(summary = "批量更新商品狀態", description = "批量上架或下架多個商品")
    @PutMapping("/batch/status")
	public ResponseEntity<Map<String, Object>> batchUpdateProductStatus(
            @RequestBody BatchUpdateProductStatusRequest request) {
		
		int updatedCount = productManagementService.batchUpdateProductStatus(request.getProductIds(), request.getIsAvailable());
		
		 Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "批量更新完成");
	        response.put("updatedCount", updatedCount);
	        
	        return ResponseEntity.ok(response);
	}
	
	//===== 商品查詢功能 =====

    /**
     * 取得所有商品（包含已下架）
     */
    @Operation(summary = "取得所有商品", description = "管理端查看所有商品，包含已下架的商品")
    @GetMapping
    public ResponseEntity<List<Products>> getAllProducts() {
        List<Products> products = productManagementService.getAllProducts();
        
        for (Products product : products) {
            System.out.println("Product: " + product.getName());
            if (product.getProductCategory() != null) {
                System.out.println("Category: " + product.getProductCategory().getCategoryName());
            } else {
                System.out.println("Category is NULL");
            }
        }
        return ResponseEntity.ok(products);
    }

    /**
     * 分頁查詢所有商品
     */
    @Operation(summary = "分頁查詢商品", description = "分頁查詢所有商品，包含已下架的商品")
    @GetMapping("/page")
    public ResponseEntity<Page<Products>> getAllProductsWithPagination(
            @Parameter(description = "頁碼，從0開始") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每頁數量") @RequestParam(defaultValue = "20") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Products> products = productManagementService.getAllProductsWithPagination(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * 根據分類查詢商品
     */
    @Operation(summary = "依分類查詢商品", description = "查詢指定分類下的所有商品（包含已下架）")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Products>> getProductsByCategory(
            @Parameter(description = "分類ID") @PathVariable Integer categoryId) {
        
        List<Products> products = productManagementService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * 搜尋所有商品
     */
    @Operation(summary = "搜尋商品", description = "根據關鍵字搜尋所有商品（包含已下架）")
    @GetMapping("/search")
    public ResponseEntity<List<Products>> searchAllProducts(
            @Parameter(description = "搜尋關鍵字") @RequestParam String keyword) {
        
        List<Products> products = productManagementService.searchAllProducts(keyword);
        return ResponseEntity.ok(products);
    }

    //===== 統計功能 =====

    /**
     * 取得商品統計資訊
     */
    @Operation(summary = "取得商品統計", description = "取得商品的整體統計資訊")
    @GetMapping("/stats")
    public ResponseEntity<ProductStatsDto> getProductStats() {
        ProductStatsDto stats = productManagementService.getProductStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 根據分類取得商品統計
     */
    @Operation(summary = "依分類取得商品統計", description = "取得指定分類下的商品統計資訊")
    @GetMapping("/stats/category/{categoryId}")
    public ResponseEntity<CategoryProductStatsDto> getProductStatsByCategory(
            @Parameter(description = "分類ID") @PathVariable Integer categoryId) {
        
        CategoryProductStatsDto stats = productManagementService.getProductStatsByCategory(categoryId);
        return ResponseEntity.ok(stats);
    }
}
