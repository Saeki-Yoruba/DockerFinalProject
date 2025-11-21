package com.supernovapos.finalproject.product.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.product.dto.MenuStructureDto;
import com.supernovapos.finalproject.product.service.MenuManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 菜單展示 API - 客戶端菜單瀏覽功能
 */
@RestController
@RequestMapping("/api/menu")
@Tag(name = "菜單展示", description = "客戶端菜單瀏覽功能")
public class MenuController {

	@Autowired
	private MenuManagementService menuManagementService;
	
//	取得客戶端菜單結構
//	只顯示啟用的分類和可用的商品
	@Operation(summary = "取得客戶端菜單", description = "取得完整的菜單結構，包含分類和商品資訊，只顯示可用項目")
    @GetMapping("/customer")
	public ResponseEntity<MenuStructureDto> getCustomerMenu(){
		MenuStructureDto menu = menuManagementService.getCustomerMenuStructure();
		return ResponseEntity.ok(menu);
	}
	
//	取得菜單統計資訊
//	提供前端顯示用的統計數據
	@Operation(summary = "取得菜單統計", description = "取得菜單的統計資訊，如分類數量、商品數量等")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMenuStats(){
		MenuStructureDto menu = menuManagementService.getCustomerMenuStructure();
		
		Map<String, Object> stats = new HashMap<>();
		stats.put("totalCategories", menu.getTotalCategories());
		stats.put("totalProducts", menu.getTotalProducts());
		stats.put("totalAvailableProducts", menu.getTotalAvailableProducts());
		stats.put("categoriesWithProducts", menu.getCategories().size());
		
		return ResponseEntity.ok(stats);
	}
	
//	檢查菜單是否可用
//	確認是否有可點餐的商品
    @Operation(summary = "檢查菜單可用性", description = "檢查目前是否有可點餐的商品")
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> checkMenuAvailability() {
    	MenuStructureDto menu = menuManagementService.getCustomerMenuStructure();
    	
    	boolean hasAvailableProducts = menu.getTotalAvailableProducts() > 0;
    	boolean hasCategories = menu.getTotalCategories() > 0;
    	
    	Map<String, Object> availability = new HashMap<>();
        availability.put("available", hasAvailableProducts);
        availability.put("hasCategories", hasCategories);
        availability.put("hasProducts", hasAvailableProducts);
        availability.put("message", hasAvailableProducts ? "菜單正常，可以點餐" : "目前暫無可點餐商品");
        
        return ResponseEntity.ok(availability);
    }
    
}
