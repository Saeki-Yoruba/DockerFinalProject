package com.supernovapos.finalproject.order.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.cart.dto.OrderGroupCartDto;
import com.supernovapos.finalproject.cart.dto.UserSpentDto;
import com.supernovapos.finalproject.cart.service.CartService;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.order.dto.AddOrderItemRequest;
import com.supernovapos.finalproject.order.dto.AddOrderRequest;
import com.supernovapos.finalproject.order.dto.OrderGroupCartStatusDto;
import com.supernovapos.finalproject.order.dto.UpdateCartItemCountRequest;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;

/**
 * 客戶端點餐 API - 處理購物車管理和訂單提交
 */
@RestController
@RequestMapping("/api/customer/order")
@Tag(name = "客戶端點餐", description = "購物車管理和訂單提交功能")
@PermitAll
public class CustomerOrderController {
	

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CartService cartService;

//	新增商品到購物車 POST /api/customer/order/{orderGroupId}/cart/items

	@Operation(summary = "新增商品到購物車", description = "將商品加入用戶購物車，支援臨時用戶和註冊用戶")
	@PostMapping("/{orderGroupId}/cart/items")
	public ResponseEntity<Map<String, String>> addItemToCart(
			@Parameter(description = "訂單群組ID") @PathVariable UUID orderGroupId,
			@Parameter(description = "用戶類型", schema = @Schema(allowableValues = { "TEMP",
					"REGISTERED" })) @RequestParam String userType,
			@Parameter(description = "用戶ID") @RequestParam String userId,
			@RequestBody AddOrderItemRequest request) {

		orderService.addItemToCart(orderGroupId, userType, userId, request);

		Map<String, String> response = new HashMap<String, String>();
		response.put("success", "true");
		response.put("message", "商品已新增到購物車");

		return ResponseEntity.ok(response);
	}

//	購物車移除商品
	@Operation(summary = "移除購物車商品", description = "從用戶購物車中移除指定商品")
	@DeleteMapping("/{orderGroupId}/cart/items/{productId}")
	public ResponseEntity<Map<String, String>> removeItemFromCart(
			@PathVariable UUID orderGroupId,
			@PathVariable Integer productId,
			@RequestParam String userType,
			@RequestParam String userId) {

		orderService.removeItemFromCart(orderGroupId, userType, userId, productId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "商品已從購物車移除");

		return ResponseEntity.ok(response);
	}

	// 更新購物車商品數量 - 加入錯誤處理 (request body需加上quantity與description)
	@Operation(summary = "更新商品數量與敘述", description = "更新購物車中指定商品的數量與敘述")
	@PutMapping("/{orderGroupId}/cart/items/{productId}")
	public ResponseEntity<Map<String, String>> updateCartItemQuantity(
	        @PathVariable UUID orderGroupId,
	        @PathVariable Integer productId,
	        @RequestParam String userType,
	        @RequestParam String userId,
	        @RequestBody UpdateCartItemCountRequest request) {

	    try {
	        orderService.updateCartItemQuantity(orderGroupId, userType, userId, productId, request);

	        Map<String, String> response = new HashMap<>();
	        response.put("success", "true");
	        
	        if (request.getQuantity() == 0) {
	            response.put("message", "商品已從購物車移除");
	        } else {
	            response.put("message", "商品數量已更新為 " + request.getQuantity());
	        }

	        return ResponseEntity.ok(response);
	        
	    } catch (ResourceNotFoundException e) {
	        Map<String, String> response = new HashMap<>();
	        response.put("success", "false");
	        response.put("message", e.getMessage());
	        
	        return ResponseEntity.badRequest().body(response);
	        
	    } catch (InvalidRequestException e) {
	        Map<String, String> response = new HashMap<>();
	        response.put("success", "false");
	        response.put("message", e.getMessage());
	        
	        return ResponseEntity.badRequest().body(response);
	    }
	}

//	清空購物車
	@Operation(summary = "清空購物車", description = "清空用戶的所有購物車內容")
	@DeleteMapping("/{orderGroupId}/cart")
	public ResponseEntity<Map<String, String>> clearCart(
			@PathVariable UUID orderGroupId,
			@RequestParam String userType,
			@RequestParam String userId) {

		orderService.clearCart(orderGroupId, userType, userId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "購物車已清空");

		return ResponseEntity.ok(response);
	}

//	取得用戶購物車的內容
	@Operation(summary = "取得購物車內容", description = "取得指定用戶的購物車詳細內容")
	@GetMapping("/{orderGroupId}/cart")
	public ResponseEntity<Orders> getUserCart(
			@PathVariable UUID orderGroupId,
			@RequestParam String userType,
			@RequestParam String userId) {

		Orders cart = orderService.getUserCartWithItems(orderGroupId, userType, userId);
		return ResponseEntity.ok(cart);
	}

//	取得整桌的購物車狀態 (所有人的購物車 + 已提交訂單)
//	這是核心功能：顯示整桌所有人的點餐狀況

	@Operation(summary = "取得整桌購物車狀態", description = "取得整桌的完整狀態，包括所有人的購物車內容和已提交訂單，用於多人協作點餐")
	@GetMapping("/{orderGroupId}/status")
	public ResponseEntity<OrderGroupCartDto> getOrderGroupCartStatus(
			@PathVariable UUID orderGroupId) {

		OrderGroupCartDto status = cartService.getOrderGroupCartStatus(orderGroupId);
		return ResponseEntity.ok(status);
	}
	
	@Operation(summary = "群組訂單小計", description = "取得整桌簡易的每個人花費計算，用於結帳成功頁面")
	@GetMapping("/{orderGroupId}/checkout")
	public ResponseEntity<List<UserSpentDto>> getOrderGroupSummary(
			@PathVariable UUID orderGroupId) {

		List<UserSpentDto> status = cartService.getOrderGroupSummary(orderGroupId);
		return ResponseEntity.ok(status);
	}

//	提交首次訂單 (將所有人的購物車一起提交)
//	核心功能：多人點餐完成後的統一提交
	@Operation(summary = "提交首次訂單", description = "將整桌所有人的購物車內容一起提交為正式訂單，只能執行一次")
	@PostMapping("/{orderGroupId}/submit-first")
	public ResponseEntity<Map<String, String>> submitFirstOrder(
			@PathVariable UUID orderGroupId,
			@RequestParam String userType,
			@RequestParam String userId) {

		orderService.submitFirstOrder(orderGroupId, userType, userId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "首次訂單已提交成功");

		return ResponseEntity.ok(response);
	}

//	加點
	@Operation(summary = "加點訂單", description = "在首次訂單提交後，新增額外的加點訂單")
	@PostMapping("/{orderGroupId}/add-order")
	public ResponseEntity<Map<String, Object>> addOrder(
			@PathVariable UUID orderGroupId,
			@RequestParam String userType,
			@RequestParam String userId,
			@RequestBody AddOrderRequest request) {
		
		Orders addOrder = orderService.addOrder(orderGroupId, userType, userId, request);
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "加點訂單已提交成功");
		response.put("order", addOrder);
		
		return ResponseEntity.ok(response);
	}
}
