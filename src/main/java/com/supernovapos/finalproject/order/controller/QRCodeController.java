package com.supernovapos.finalproject.order.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.common.model.ApiResponse;
import com.supernovapos.finalproject.order.dto.OrderGroupDetailDto;
import com.supernovapos.finalproject.order.dto.TablePaymentRequest;
import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.OrderItems;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.OrdersRepository;
import com.supernovapos.finalproject.order.service.OrderGroupService;
import com.supernovapos.finalproject.order.service.OrderItemsService;
import com.supernovapos.finalproject.order.service.OrderService;
import com.supernovapos.finalproject.table.model.RestaurantTable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * QR Code 相關 API - 處理桌子 QR Code 掃描和訂單群組管理
 */
@RestController
@RequestMapping("/api/qr")
@Tag(name = "QR Code 管理", description = "QR 碼生成、驗證和訂單群組管理")
public class QRCodeController {

	@Autowired
	private OrderGroupService orderGroupService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderItemsService orderItemsService;

	@Autowired
	private OrdersRepository ordersRepository;

	@Autowired
	private OrderGroupRepository orderGroupRepository;

//	店家端：為指定桌子生成新的點餐會話 (產生 QR Code)
	@Operation(summary = "生成點餐 QR Code", description = "店家端為指定桌號生成新的點餐會話")
	@PostMapping("/generate/{tableId}")
	public ResponseEntity<Map<String, Object>> generateQRCode(
			@PathVariable Integer tableId) {

		OrderGroup orderGroup = orderGroupService.createNewSession(tableId);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "成功為桌號 " + tableId + " 建立點餐會話");
		response.put("orderGroupId", orderGroup.getId());
		response.put("tableId", tableId);
		response.put("qrCodeUrl", "/order/" + orderGroup.getId());
		response.put("orderGroup", orderGroup);

		return ResponseEntity.ok(response);
	}

//	客戶端：掃描 QR Code 後驗證訂單群組
	@Operation(summary = "驗證 QR Code", description = "客戶端掃描 QR Code 後驗證訂單群組有效性")
	@GetMapping("/verify/{orderGroupId}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> verifyQRCode(
			@Parameter(description = "訂單群組ID") @PathVariable UUID orderGroupId) {

		OrderGroup orderGroup = orderGroupService.findByQrCode(orderGroupId);
		RestaurantTable table = orderGroupService.getTableByOrderGroupId(orderGroupId);

		Map<String, Object> payload = new HashMap<>();
		payload.put("orderGroupId", orderGroupId);
		payload.put("tableId", table.getId());
		payload.put("tableName", "桌號 " + table.getTableId() + " (容量: " + table.getCapacity() + "人)");
		payload.put("canSubmitFirstOrder", orderGroupService.canSubmitFirstOrder(orderGroupId));
		payload.put("canAddOrder", orderGroupService.canAddOrder(orderGroupId));
		payload.put("status", orderGroup.getStatus());
		payload.put("createdAt", orderGroup.getCreatedAt());

		return ResponseEntity.ok(
				new ApiResponse<>(true, "QR Code 驗證成功", payload));
	}

//	客戶端：取得訂單群組詳細資訊
	@Operation(summary = "取得訂單群組詳情", description = "取得訂單群組的完整資訊，包含所有訂單")
	@GetMapping("/details/{orderGroupId}")
	public ResponseEntity<OrderGroupDetailDto> getOrderGroupDetails(
			@Parameter(description = "訂單群組ID") @PathVariable UUID orderGroupId) {

		OrderGroupDetailDto details = orderGroupService.getOrderGroupDetail(orderGroupId);
		return ResponseEntity.ok(details);
	}

//	店家端：完成訂單群組 (結帳)
	@Operation(summary = "完成結帳", description = "店家端完成訂單群組結帳")
	@PutMapping("/complete/{orderGroupId}")
	public ResponseEntity<Map<String, String>> completeOrderGroup(
			@Parameter(description = "訂單群組ID") @PathVariable UUID orderGroupId) {

		orderGroupService.completeOrderGroup(orderGroupId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "訂單群組已完成結帳");

		return ResponseEntity.ok(response);
	}

//	店家端：強制結束訂單群組 (異常情況處理)
	@Operation(summary = "強制結束訂單", description = "強制結束訂單群組，用於異常情況處理")
	@PutMapping("/force-complete/{orderGroupId}")
	public ResponseEntity<Map<String, String>> forceCompleteOrderGroup(
			@Parameter(description = "訂單群組ID") @PathVariable UUID orderGroupId) {

		orderGroupService.forceCompleteOrderGroup(orderGroupId);

		Map<String, String> response = new HashMap<>();
		response.put("success", "true");
		response.put("message", "訂單群組已強制結束");

		return ResponseEntity.ok(response);
	}

//	檢查桌子是否有進行中的訂單
	@Operation(summary = "檢查桌子狀態", description = "檢查指定桌號是否有進行中的訂單")
	@GetMapping("/table/{tableId}/status")
	public ResponseEntity<Map<String, Object>> checkTableStatus(
			@Parameter(description = "餐桌號碼") @PathVariable Integer tableId) {

		boolean hasActiveOrder = orderGroupService.hasActiveOrderByTableId(tableId);

		Map<String, Object> response = new HashMap<>();
		response.put("tableId", tableId);
		response.put("hasActiveOrder", hasActiveOrder);

		return ResponseEntity.ok(response);
	}

	// ===== 新增：訂單管理相關 API (簡化版) =====

	/**
	 * 取得當前所有活躍訂單詳細資料
	 */
	@Operation(summary = "取得當前所有活躍訂單群組詳細資料", description = "取得所有活躍中的訂單群組，包含該群組內的所有訂單，按桌位分組顯示")
	@GetMapping("/active-orders-detail")
	public ResponseEntity<Map<String, Object>> getActiveOrders() {
		// 取得所有活躍的訂單群組
		List<OrderGroup> activeOrderGroups = orderGroupService.getActiveOrderGroups();
		Map<String, List<Map<String, Object>>> groupedOrders = new HashMap<>();

		for (OrderGroup orderGroup : activeOrderGroups) {
			RestaurantTable table = orderGroup.getTable();
			String tableName = table.getTableId().toString();

			// 修改：只取得該訂單群組的已提交訂單（不包括草稿）
			List<Orders> submittedOrders = ordersRepository.findSubmittedOrdersByGroupId(orderGroup.getId());

			List<Map<String, Object>> orderList = new ArrayList<>();
			for (Orders order : submittedOrders) {
				Map<String, Object> orderData = new HashMap<>();
				orderData.put("id", order.getId());
				orderData.put("orderNo", "訂單 ＃" + order.getId());
				orderData.put("total", order.getTotalAmount());
				// 方案1：使用 note 欄位判斷（最簡單，不需要修改資料庫）
				boolean isServed = order.getNote() != null && order.getNote().contains("已出餐");
				orderData.put("done", isServed);
				orderData.put("createdAt", formatDateTime(order.getCreatedAt()));
				orderData.put("note", order.getNote());

				// 取得訂單項目
				List<OrderItems> orderItems = orderItemsService.getOrderItemsByOrderId(order.getId());
				List<Map<String, Object>> itemList = new ArrayList<>();

				for (OrderItems item : orderItems) {
					Map<String, Object> itemData = new HashMap<>();
					itemData.put("id", item.getId());
					itemData.put("productName", item.getProducts().getName());
					itemData.put("quantity", item.getQuantity());
					itemData.put("unitPrice", item.getUnitPrice());
					itemData.put("subtotal", item.getSubtotal());
					itemData.put("note", item.getNote());
					itemList.add(itemData);
				}

				orderData.put("orderItems", itemList);
				orderList.add(orderData);
			}

			if (!orderList.isEmpty()) {
				groupedOrders.put(tableName, orderList);
			}
		}

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("data", groupedOrders);

		return ResponseEntity.ok(response);
	}

	/**
	 * 格式化日期時間
	 */
	private String formatDateTime(java.time.LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		return dateTime.format(formatter);
	}

	/**
	 * 切換訂單出餐狀態
	 */
	@Operation(summary = "切換訂單出餐狀態", description = "切換指定訂單的出餐狀態（已出餐/未出餐）")
	@PutMapping("/order/{orderId}/toggle-status")
	public ResponseEntity<Map<String, Object>> toggleOrderStatus(
			@Parameter(description = "訂單ID") @PathVariable Long orderId) {

		// 查找訂單
		Optional<Orders> orderOpt = ordersRepository.findById(orderId);
		if (!orderOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單不存在");
		}

		Orders order = orderOpt.get();

		// 檢查當前出餐狀態（從 note 欄位判斷）
		boolean isCurrentlyServed = order.getNote() != null && order.getNote().contains("已出餐");
		boolean newStatus = !isCurrentlyServed;

		// 更新 note 欄位來儲存出餐狀態
		String currentNote = order.getNote() == null ? "" : order.getNote();

		if (newStatus) {
			// 標記為已出餐
			if (!currentNote.contains("已出餐")) {
				String newNote = currentNote.trim().isEmpty() ? "[已出餐]" : currentNote + " [已出餐]";
				order.setNote(newNote);
			}
		} else {
			// 標記為未出餐（移除已出餐標記）
			String newNote = currentNote.replace(" [已出餐]", "").replace("[已出餐]", "").trim();
			order.setNote(newNote.isEmpty() ? null : newNote);
		}

		// 保存到資料庫
		ordersRepository.save(order);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "訂單 #" + orderId + " 已" + (newStatus ? "出餐" : "改為未出餐"));
		response.put("orderId", orderId);
		response.put("isServed", newStatus);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "整桌收款", description = "處理整桌的收款作業並將訂單群組設為不活躍")
	@PostMapping("/payment/table")
	public ResponseEntity<Map<String, Object>> processTablePayment(
			@RequestBody TablePaymentRequest request) {

		try {
			// 從桌號解析出桌子ID 
			String tableIdStr = request.getTable();
			Integer tableId = Integer.parseInt(tableIdStr);

			// 找到該桌的活躍訂單群組
			OrderGroup activeOrderGroup = orderGroupService.getActiveOrderGroupByTableId(tableId);

			// 只檢查已提交的訂單（草稿訂單不需要檢查出餐狀態）
	        List<Orders> submittedOrders = ordersRepository.findSubmittedOrdersByGroupId(activeOrderGroup.getId());
			List<String> unfinishedOrders = new ArrayList<>();

			for (Orders order : submittedOrders) {
				// 修改：使用 note 欄位判斷是否已出餐
	            boolean isServed = order.getNote() != null && order.getNote().contains("已出餐");
	            
	            // 如果訂單未出餐，加入未完成列表
	            if (!isServed) {
	                unfinishedOrders.add("訂單 #" + order.getId());
	            }
	        }

			// 如果有未出餐的訂單，拒絕收款
			if (!unfinishedOrders.isEmpty()) {
				Map<String, Object> response = new HashMap<>();
				response.put("success", false);
				response.put("message", "無法完成收款，還有以下訂單尚未出餐");
				response.put("unfinishedOrders", unfinishedOrders);
				response.put("totalUnfinished", unfinishedOrders.size());

				return ResponseEntity.badRequest().body(response);
			}

			// 所有訂單都已出餐，可以進行收款
			orderGroupService.completeOrderGroup(activeOrderGroup.getId());

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "桌號 " + request.getTable() + " 收款完成，金額：$" + request.getTotal());
			response.put("completedOrderGroupId", activeOrderGroup.getId());
			response.put("totalAmount", request.getTotal());

			return ResponseEntity.ok(response);

		} catch (ResourceNotFoundException e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "找不到桌號 " + request.getTable() + " 的活躍訂單");

			return ResponseEntity.badRequest().body(response);

		} catch (NumberFormatException e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "桌號格式錯誤：" + request.getTable());

			return ResponseEntity.badRequest().body(response);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "收款失敗：" + e.getMessage());

			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * 取得當前所有活躍訂單群組
	 */
	@Operation(summary = "取得當前所有活躍訂單群組", description = "取得所有活躍中的訂單群組基本資訊，用於QR碼管理")
	@GetMapping("/active-orders")
	public ResponseEntity<List<Map<String, Object>>> getActiveOrderGroups() {
		try {
			// 取得所有活躍的訂單群組
			List<OrderGroup> activeOrderGroups = orderGroupService.getActiveOrderGroups();
			List<Map<String, Object>> result = new ArrayList<>();

			for (OrderGroup orderGroup : activeOrderGroups) {
				Map<String, Object> groupData = new HashMap<>();

				// 基本訂單群組資訊
				groupData.put("id", orderGroup.getId());
				groupData.put("totalAmount", orderGroup.getTotalAmount());
				groupData.put("status", orderGroup.getStatus());
				groupData.put("hasOrder", orderGroup.getHasOrder());
				groupData.put("createdAt", orderGroup.getCreatedAt());
				groupData.put("updatedAt", orderGroup.getUpdatedAt());

				// 桌子資訊
				RestaurantTable table = orderGroup.getTable();
				if (table != null) {
					Map<String, Object> tableData = new HashMap<>();
					tableData.put("id", table.getId());
					tableData.put("tableId", table.getTableId());
					tableData.put("capacity", table.getCapacity());
					tableData.put("isAvailable", table.getIsAvailable());
					groupData.put("table", tableData);
				}

				// 統計該群組的訂單數量
				List<Orders> allOrders = ordersRepository.findAllOrdersByGroupId(orderGroup.getId());
				groupData.put("ordersCount", allOrders.size());

				// 統計已提交和草稿訂單數量
				long submittedOrdersCount = allOrders.stream()
						.filter(Orders::getStatus)
						.count();
				long draftOrdersCount = allOrders.stream()
						.filter(order -> !order.getStatus())
						.count();

				groupData.put("submittedOrdersCount", submittedOrdersCount);
				groupData.put("draftOrdersCount", draftOrdersCount);

				result.add(groupData);
			}

			return ResponseEntity.ok(result);

		} catch (Exception e) {
			// 錯誤處理
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "載入活躍訂單群組失敗: " + e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(List.of(errorResponse));
		}
	}
}
