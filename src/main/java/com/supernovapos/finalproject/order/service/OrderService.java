package com.supernovapos.finalproject.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.order.dto.AddOrderItemRequest;
import com.supernovapos.finalproject.order.dto.AddOrderRequest;
import com.supernovapos.finalproject.order.dto.CartItemDto;
import com.supernovapos.finalproject.order.dto.OrderGroupCartStatusDto;
import com.supernovapos.finalproject.order.dto.UpdateCartItemCountRequest;
import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.OrderItems;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.model.TempUser;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.OrderItemsRepository;
import com.supernovapos.finalproject.order.repository.OrdersRepository;
import com.supernovapos.finalproject.order.repository.TempUserRepository;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.repository.ProductsRepository;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// 訂單管理核心服務

@Service
@Transactional
public class OrderService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private OrdersRepository ordersRepository;

	@Autowired
	private OrderGroupRepository orderGroupRepository;

	@Autowired
	private OrderItemsRepository orderItemsRepository;

	@Autowired
	private TempUserRepository tempUserRepository;

	@Autowired
	private ProductsRepository productsRepository;

	@Autowired
	private UserRepository userRepository;

// ================= 統一的公開方法 =================	

//	取得或創建用戶購物車(統一方法)
	public Orders getOrCreateUserCart(UUID groupId, String userType, String userId) {
		if ("TEMP".equals(userType)) {
			UUID tempUserId = UUID.fromString(userId);
			return getOrCreateTempUserCart(groupId, tempUserId);
		} else if ("REGISTERED".equals(userType)) {
			long registeredUserId = Long.parseLong(userId);
			return getOrCreateRegisteredUserCart(groupId, registeredUserId);
		} else {
			throw new InvalidRequestException("不支援的用戶類型:" + userType);
		}
	}

//	添加商品到購物車(統一方法)
	public void addItemToCart(UUID groupId, String userType, String userId, AddOrderItemRequest request) {
		// 取得購物車
		Orders cart = getOrCreateUserCart(groupId, userType, userId);

		// 檢查商品是否存在且可用
		Optional<Products> productOpt = productsRepository.findById(request.getProductId());
		if (!productOpt.isPresent()) {
			throw new ResourceNotFoundException("商品不存在");
		}
		Products product = productOpt.get();

		if (!product.getIsAvailable()) {
			throw new InvalidRequestException("商品" + product.getName() + "已下架");
		}

		// 檢查購物車中是否已有相同商品
		List<OrderItems> existingItems = orderItemsRepository.findOrderItemsWithProductsByOrderId(cart.getId());
		OrderItems existingItem = null;

		for (OrderItems item : existingItems) {
			if (item.getProducts().getId().equals(request.getProductId())) {
				existingItem = item;
				break;
			}
		}

		if (existingItem != null) {
			// 更新數量
			existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
			orderItemsRepository.save(existingItem);
		} else {
			// 創建新的訂單項目
			OrderItems newItem = new OrderItems();
			newItem.setOrders(cart);
			newItem.setProducts(product);
			newItem.setQuantity(request.getQuantity());
			newItem.setUnitPrice(product.getPrice());
			newItem.setNote(request.getNote());

			orderItemsRepository.save(newItem);
		}

		// 重新計算購物車總額
		updateOrderTotalAmount(cart.getId());
	}

//	@Transactional
//	public void removeItemFromCart(UUID groupId, String userType, String userId, Integer productId) {
//	    System.out.println("=== 詳細除錯開始 ===");
//	    System.out.println("要移除的商品ID: " + productId);
//	    
//	    Orders cart = getOrCreateUserCart(groupId, userType, userId);
//	    System.out.println("購物車ID: " + cart.getId());
//	    System.out.println("購物車總額: " + cart.getTotalAmount());
//
//	    List<OrderItems> items = orderItemsRepository.findOrderItemsWithProductsByOrderId(cart.getId());
//	    System.out.println("購物車商品項目數量: " + items.size());
//	    
//	    // 列出所有商品的詳細資訊
//	    for (int i = 0; i < items.size(); i++) {
//	        OrderItems item = items.get(i);
//	        if (item.getProducts() != null) {
//	            System.out.println("商品 " + i + ":");
//	            System.out.println("  ItemID: " + item.getId());
//	            System.out.println("  ProductID: " + item.getProducts().getId());
//	            System.out.println("  ProductName: " + item.getProducts().getName());
//	            System.out.println("  Quantity: " + item.getQuantity());
//	            System.out.println("  UnitPrice: " + item.getUnitPrice());
//	            System.out.println("  Subtotal: " + item.getSubtotal());
//	        }
//	    }
//
//	    boolean itemFound = false;
//	    for (OrderItems item : items) {
//	        if (item.getProducts() != null && item.getProducts().getId().equals(productId)) {
//	            System.out.println("🎯 找到要刪除的商品:");
//	            System.out.println("  ItemID: " + item.getId());
//	            System.out.println("  ProductID: " + item.getProducts().getId());
//	            System.out.println("  ProductName: " + item.getProducts().getName());
//	            
//	            orderItemsRepository.deleteById(item.getId());
//	            orderItemsRepository.flush();
//	            entityManager.clear();
//	            System.out.println("✅ 商品項目已刪除");
//	            itemFound = true;
//	            break;
//	        }
//	    }
//
//	    if (!itemFound) {
//	        System.out.println("❌ 沒有找到商品ID: " + productId);
//	        System.out.println("購物車中的商品ID列表:");
//	        for (OrderItems item : items) {
//	            if (item.getProducts() != null) {
//	                System.out.println("  - " + item.getProducts().getId());
//	            }
//	        }
//	        throw new ResourceNotFoundException("購物車中找不到指定的商品（商品ID: " + productId + "）");
//	    }
//
//	    // 重新查詢確認
//	    List<OrderItems> itemsAfter = orderItemsRepository.findOrderItemsWithProductsByOrderId(cart.getId());
//	    System.out.println("移除後商品項目數量: " + itemsAfter.size());
//
//	    // 重新計算購物車總額
//	    updateOrderTotalAmount(cart.getId());
//	    
//	    // 查詢更新後的購物車
//	    Optional<Orders> updatedCartOpt = ordersRepository.findById(cart.getId());
//	    if (updatedCartOpt.isPresent()) {
//	        Orders updatedCart = updatedCartOpt.get();
//	        System.out.println("更新後購物車總額: " + updatedCart.getTotalAmount());
//	    }
//	    
//	    System.out.println("=== 詳細除錯結束 ===");
//	}
	
	public void removeItemFromCart(UUID groupId, String userType, String userId, Integer productId) {
	    Orders cart = getOrCreateUserCart(groupId, userType, userId);

	    // 直接從 cart.orderItems 找
	    List<OrderItems> items = cart.getOrderItems();

	    OrderItems target = null;
	    for (OrderItems item : items) {
	        if (item.getProducts() != null && item.getProducts().getId().equals(productId)) {
	            target = item;
	            break;
	        }
	    }

	    if (target == null) {
	        throw new ResourceNotFoundException("購物車中找不到指定的商品（商品ID: " + productId + "）");
	    }

	    // 從集合移除，orphanRemoval 會觸發 DELETE
	    cart.getOrderItems().remove(target);

	    // 儲存 cart，JPA 會自動刪掉孤兒 OrderItems
	    ordersRepository.save(cart);

	    // 更新總額
	    updateOrderTotalAmount(cart.getId());
	}
//	// 從購物車移除商品(統一方法) - 加入商品存在檢查
//	@Transactional
//	public void removeItemFromCart(UUID groupId, String userType, String userId, Integer productId) {
//		Orders cart = getOrCreateUserCart(groupId, userType, userId);
//
//		List<OrderItems> items = orderItemsRepository.findOrderItemsWithProductsByOrderId(cart.getId());
//
//		// 檢查購物車中是否有該商品
//		boolean itemFound = false;
//		for (OrderItems item : items) {
//			if (item.getProducts() != null && item.getProducts().getId().equals(productId)) {
//				// 改用 deleteById
//				orderItemsRepository.deleteById(item.getId());
//				orderItemsRepository.flush(); // 強制刷新
//				entityManager.clear(); // 清除一級緩存
//				itemFound = true;
//				break;
//			}
//		}
//		// 如果找不到商品，拋出例外
//		if (!itemFound) {
//			throw new ResourceNotFoundException("購物車中找不到指定的商品（商品ID: " + productId + "）");
//		}
//		// 重新計算購物車總額
//		updateOrderTotalAmount(cart.getId());
//	}

	// 更新購物車商品數量(統一方法) - 加入商品存在檢查
	public void updateCartItemQuantity(UUID groupId, String userType, String userId, Integer productId,
			UpdateCartItemCountRequest request) {
		// 檢查數量是否有效
		if (request.getQuantity() < 0) {
			throw new InvalidRequestException("商品數量不能小於 0");
		}

		// 如果數量為 0，直接移除商品
		if (request.getQuantity() == 0) {
			removeItemFromCart(groupId, userType, userId, productId);
			return;
		}

		Orders cart = getOrCreateUserCart(groupId, userType, userId);
		List<OrderItems> items = orderItemsRepository.findOrderItemsWithProductsByOrderId(cart.getId());

		// 檢查購物車中是否有該商品
		boolean itemFound = false;
		for (OrderItems item : items) {
			if (item.getProducts().getId().equals(productId)) {
				item.setQuantity(request.getQuantity());
				item.setNote(request.getNote());
				orderItemsRepository.save(item);
				itemFound = true;
				break;
			}
		}

		// 如果找不到商品，拋出例外
		if (!itemFound) {
			throw new ResourceNotFoundException("購物車中找不到指定的商品（商品ID: " + productId + "）");
		}

		// 重新計算購物車總額
		updateOrderTotalAmount(cart.getId());
	}

//	清空購物車(統一方法)
	public void clearCart(UUID groupId, String userType, String userId) {
		Orders cart = getOrCreateUserCart(groupId, userType, userId);

		// 刪除所有訂單項目
		orderItemsRepository.deleteByOrderId(cart.getId());

		// 重設總額為 0
		cart.setTotalAmount(0);
		ordersRepository.save(cart);
	}

//	取得用戶的購物車(包含商品詳情) (統一方法)
	public Orders getUserCartWithItems(UUID groupId, String userType, String userId) {
		// 直接使用會自動載入所有資料的方法
		Orders cart = getOrCreateUserCart(groupId, userType, userId);
		return cart;
	}

//	提交首次訂單（統一方法 - 支援任何用戶類型發起）
	public void submitFirstOrder(UUID groupId, String userType, String userId) {
		// 檢查是否可以提交
		Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findActiveOrderGroup(groupId);
		if (!orderGroupOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單群組不存在");
		}
		OrderGroup orderGroup = orderGroupOpt.get();

		if (orderGroup.getHasOrder()) {
			throw new ConflictException("已有人提交過訂單");
		}

		// 檢查是否有草稿訂單
		List<Orders> draftOrders = ordersRepository.findDraftOrdersByGroupId(groupId);
		if (draftOrders.isEmpty()) {
			throw new InvalidRequestException("沒有可提交的訂單項目");
		}

		// 驗證提交用戶是否有草稿訂單
		boolean userHasDraftOrder = false;
		for (Orders order : draftOrders) {
			if (isOrderBelongsToUser(order, userType, userId)) {
				userHasDraftOrder = true;
				break;
			}
		}

		if (!userHasDraftOrder) {
			throw new InvalidRequestException("您沒有待提交的訂單");
		}

		// 批量更新所有草稿訂單為已提交
		Integer updatedCount = ordersRepository.updateDraftOrdersToSubmitted(groupId);
		if (updatedCount == 0) {
			throw new InvalidRequestException("沒有可提交的訂單");
		}
		// 標記訂單群組已有人提交
		orderGroup.setHasOrder(true);

		// 重新計算總金額
		Integer totalAmount = ordersRepository.sumTotalAmountByGroupId(groupId);
		orderGroup.setTotalAmount(totalAmount);

		orderGroupRepository.save(orderGroup);
	}

//	加點
	public Orders addOrder(UUID groupId, String userType, String userId, AddOrderRequest request) {
		// 檢查是否可以加點
		Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findActiveOrderGroup(groupId);
		if (!orderGroupOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單群組不存在");
		}
		OrderGroup orderGroup = orderGroupOpt.get();

		if (!orderGroup.getHasOrder()) {
			throw new InvalidRequestException("尚未有人提交首次訂單，無法加點");
		}

		// 創建加點訂單(直接設為已提交)
		Orders additionalOrder = new Orders();
		additionalOrder.setOrderGroup(orderGroup);
		additionalOrder.setStatus(true);
		additionalOrder.setTotalAmount(0);
		additionalOrder.setNote(request.getNote());

		// 設定用戶關聯
		if ("TEMP".equals(userType)) {
			UUID tempUserId = UUID.fromString(userId);
			Optional<TempUser> tempUserOpt = tempUserRepository.findById(tempUserId);
			if (!tempUserOpt.isPresent()) {
				throw new ResourceNotFoundException("臨時用戶不存在");
			}
			additionalOrder.setTempUser(tempUserOpt.get());
		} else if ("REGISTERED".equals(userType)) {
			Long registeredUserId = Long.parseLong(userId);
			Optional<User> userOpt = userRepository.findById(registeredUserId);
			if (!userOpt.isPresent()) {
				throw new ResourceNotFoundException("註冊用戶不存在");
			}
			User user = userOpt.get();

			if (!user.getIsActive()) {
				throw new InvalidRequestException("用戶帳號已停用");
			}
			additionalOrder.setUser(user);
		} else {
			throw new InvalidRequestException("不支援的用戶類型: " + userType);
		}

		Orders savedOrder = ordersRepository.save(additionalOrder);

		// 添加訂單項目
		List<AddOrderItemRequest> items = request.getItems();
		for (AddOrderItemRequest itemRequest : items) {
			addOrderItemToOrder(savedOrder.getId(), itemRequest);
		}

		// 重新計算訂單總額
		updateOrderTotalAmount(savedOrder.getId());

		// 更新訂單群組總額
		updateOrderGroupTotalAmount(groupId);

		Optional<Orders> updatedOrderOpt = ordersRepository.findById(savedOrder.getId());
		return updatedOrderOpt.isPresent() ? updatedOrderOpt.get() : savedOrder;
	}

//	取得特定用戶的購物車
	public Orders getUserCart(UUID groupId, String userType, String userId) {
		return getOrCreateUserCart(groupId, userType, userId);
	}

//	================= 查詢方法 =================		

//	取得購物車內容
	public List<Orders> getShoppingCartContents(UUID groupId) {
		return ordersRepository.findDraftOrdersByGroupId(groupId);
	}

//	取得訂單群組的所有購物車內容
	public List<CartItemDto> getUnifiedShoppingCartContents(UUID groupId) {
		List<Orders> draftOrders = ordersRepository.findDraftOrdersByGroupId(groupId);
		List<CartItemDto> cartItems = new ArrayList<>();

		for (Orders order : draftOrders) {
			List<OrderItems> items = orderItemsRepository.findOrderItemsWithProductsByOrderId(order.getId());

			CartItemDto cartItem = new CartItemDto();
			cartItem.setOrderId(order.getId());
			cartItem.setTotalAmount(order.getTotalAmount());
			cartItem.setNote(order.getNote());
			cartItem.setCreatedAt(order.getCreatedAt());
			cartItem.setOrderItems(items);

			// 設定用戶資訊
			if (order.getTempUser() != null) {
				cartItem.setUserType("TEMP");
				cartItem.setUserId(order.getTempUser().getId().toString());
				cartItem.setUserNickname(order.getTempUser().getNickname());
			} else if (order.getUser() != null) {
				cartItem.setUserType("REGISTERED");
				cartItem.setUserId(order.getUser().getId().toString());
				String nickname = order.getUser().getNickname();
				if (nickname == null || nickname.isBlank()) {
				    nickname = "用戶" + order.getUser().getId();
				}
				cartItem.setUserNickname(nickname);
			} else {
				cartItem.setUserType("UNKNOWN");
				cartItem.setUserId("unknown");
				cartItem.setUserNickname("未知用戶");
			}
			cartItems.add(cartItem);
		}
		return cartItems;
	}

//	取得已提交訂單
	public List<Orders> getSubmittedOrders(UUID groupId) {
		return ordersRepository.findSubmittedOrdersByGroupId(groupId);
	}

//	取得訂單群組的所有已提交訂單
	public List<CartItemDto> getUnifiedSubmittedOrders(UUID groupId) {
		List<Orders> submittedOrders = ordersRepository.findSubmittedOrdersByGroupId(groupId);
		List<CartItemDto> orderItems = new ArrayList<>();

		for (Orders order : submittedOrders) {
			List<OrderItems> items = orderItemsRepository.findOrderItemsWithProductsByOrderId(order.getId());

			CartItemDto orderItem = new CartItemDto();
			orderItem.setOrderId(order.getId());
			orderItem.setTotalAmount(order.getTotalAmount());
			orderItem.setNote(order.getNote());
			orderItem.setCreatedAt(order.getCreatedAt());
			orderItem.setSubmittedAt(order.getUpdatedAt());
			orderItem.setOrderItems(items);

			// 設定用戶資訊
			if (order.getTempUser() != null) {
				orderItem.setUserType("TEMP");
				orderItem.setUserId(order.getTempUser().getId().toString());
				orderItem.setUserNickname(order.getTempUser().getNickname());
			} else if (order.getUser() != null) {
				orderItem.setUserType("REGISTERED");
				orderItem.setUserId(order.getUser().getId().toString());
				String nickname = order.getUser().getNickname();
				if (nickname == null || nickname.isBlank()) {
				    nickname = "用戶" + order.getUser().getId();
				}
				orderItem.setUserNickname(nickname);
			} else {
				orderItem.setUserType("UNKNOWN");
				orderItem.setUserId("unknown");
				orderItem.setUserNickname("未知用戶");
			}
			orderItems.add(orderItem);
		}
		return orderItems;
	}

// 取得訂單群組的完整狀況（購物車 + 已提交訂單）
	public OrderGroupCartStatusDto getOrderGroupCartStatus(UUID groupId) {
		OrderGroup orderGroup = orderGroupRepository.findActiveOrderGroup(groupId)
				.orElseThrow(() -> new ResourceNotFoundException("訂單組不存在或已過期"));

		OrderGroupCartStatusDto status = new OrderGroupCartStatusDto();
		status.setGroupId(groupId);
		status.setTableId(orderGroup.getTable().getTableId());
		// 防護措施：確保回傳的列表不為 null
		List<CartItemDto> cartItems = getUnifiedShoppingCartContents(groupId);
		List<CartItemDto> submittedOrders = getUnifiedSubmittedOrders(groupId);
		
		status.setCartItems(cartItems != null ? cartItems : new ArrayList<>());
		status.setSubmittedOrders(submittedOrders != null ? submittedOrders : new ArrayList<>());

		// 計算統計資訊
		Integer totalCartAmount = 0;
		Integer totalSubmittedAmount = 0;

		for (CartItemDto cartItem : status.getCartItems()) {
			totalCartAmount += cartItem.getTotalAmount();
		}

		for (CartItemDto submittedOrder : status.getSubmittedOrders()) {
			totalSubmittedAmount += submittedOrder.getTotalAmount();
		}

		status.setTotalCartAmount(totalCartAmount);
		status.setTotalSubmittedAmount(totalSubmittedAmount);
		status.setGrandTotal(totalCartAmount + totalSubmittedAmount);

		// 設定操作權限狀態
		status.setCanSubmitFirstOrder(!orderGroup.getHasOrder() && orderGroup.getStatus());
		status.setCanAddOrder(orderGroup.getHasOrder() && orderGroup.getStatus());

		return status;
	}

//	================= 內部私有方法 =================	

	// 取得或創建臨時用戶的購物車
	private Orders getOrCreateTempUserCart(UUID groupId, UUID tempUserId) {
		// 先用新的查詢方法嘗試取得購物車
		Optional<Orders> existingCart = ordersRepository.findTempUserCartWithAllData(groupId, tempUserId);
		if (existingCart.isPresent()) {
			return existingCart.get();
		}

		// 如果沒有購物車，就創建新的（原本的邏輯）
		Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findActiveOrderGroup(groupId);
		if (!orderGroupOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單群組不存在或已過期");
		}
		OrderGroup orderGroup = orderGroupOpt.get();

		Optional<TempUser> tempUserOpt = tempUserRepository.findById(tempUserId);
		if (!tempUserOpt.isPresent()) {
			throw new ResourceNotFoundException("臨時用戶不存在");
		}
		TempUser tempUser = tempUserOpt.get();

		// 驗證臨時用戶是否屬於此訂單組
		if (!tempUser.getOrderGroup().getId().equals(groupId)) {
			throw new InvalidRequestException("臨時用戶不屬於此訂單組");
		}

		Orders newCart = new Orders();
		newCart.setOrderGroup(orderGroup);
		newCart.setTempUser(tempUser);
		newCart.setStatus(false);
		newCart.setTotalAmount(0);

		return ordersRepository.save(newCart);
	}

	// 取得或創建註冊用戶的購物車
	private Orders getOrCreateRegisteredUserCart(UUID groupId, Long userId) {
		// 先用新的查詢方法嘗試取得購物車
		Optional<Orders> existingCart = ordersRepository.findRegisteredUserCartWithAllData(groupId, userId);
		if (existingCart.isPresent()) {
			return existingCart.get();
		}

		// 如果沒有購物車，就創建新的（原本的邏輯）
		Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findActiveOrderGroup(groupId);
		if (!orderGroupOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單群組不存在或已過期");
		}
		OrderGroup orderGroup = orderGroupOpt.get();

		Optional<User> userOpt = userRepository.findById(userId);
		if (!userOpt.isPresent()) {
			throw new ResourceNotFoundException("註冊用戶不存在");
		}
		User user = userOpt.get();

		Orders newCart = new Orders();
		newCart.setOrderGroup(orderGroup);
		newCart.setUser(user);
		newCart.setStatus(false);
		newCart.setTotalAmount(0);

		return ordersRepository.save(newCart);
	}

//	更新訂單總金額
	private void updateOrderTotalAmount(Long orderId) {
		List<OrderItems> items = orderItemsRepository.findOrderItemsWithProductsByOrderId(orderId);

		int totalAmount = 0;
		for (OrderItems item : items) {
			int itemTotal = item.getQuantity() * item.getUnitPrice().intValue();
			totalAmount += itemTotal;
		}

		Optional<Orders> orderOpt = ordersRepository.findById(orderId);
		if (orderOpt.isPresent()) {
			Orders order = orderOpt.get();
			order.setTotalAmount(totalAmount);
			ordersRepository.save(order);
		}
	}

// 	更新訂單群組總金額
	private void updateOrderGroupTotalAmount(UUID groupId) {
		Integer totalAmount = ordersRepository.sumTotalAmountByGroupId(groupId);

		Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findById(groupId);
		if (orderGroupOpt.isPresent()) {
			OrderGroup orderGroup = orderGroupOpt.get();
			orderGroup.setTotalAmount(totalAmount);
			orderGroupRepository.save(orderGroup);
		}
	}

//	添加訂單項目到指定訂單
	private void addOrderItemToOrder(Long orderId, AddOrderItemRequest request) {
		Optional<Orders> orderOpt = ordersRepository.findById(orderId);
		if (!orderOpt.isPresent()) {
			throw new ResourceNotFoundException("訂單不存在");
		}
		Orders order = orderOpt.get();

		Optional<Products> productOpt = productsRepository.findById(request.getProductId());
		if (!productOpt.isPresent()) {
			throw new ResourceNotFoundException("商品不存在");
		}
		Products product = productOpt.get();

		OrderItems orderItem = new OrderItems();
		orderItem.setOrders(order);
		orderItem.setProducts(product);
		orderItem.setQuantity(request.getQuantity());
		orderItem.setUnitPrice(product.getPrice());
		orderItem.setNote(request.getNote());

		orderItemsRepository.save(orderItem);
	}

	private boolean isOrderBelongsToUser(Orders order, String userType, String userId) {
		if ("TEMP".equals(userType)) {
			return order.getTempUser() != null &&
					order.getTempUser().getId().toString().equals(userId);
		} else if ("REGISTERED".equals(userType)) {
			return order.getUser() != null &&
					order.getUser().getId().toString().equals(userId);
		}
		return false;
	}
}