package com.supernovapos.finalproject.payment.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.OrderItems;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.OrdersRepository;
import com.supernovapos.finalproject.payment.model.Payment;
import com.supernovapos.finalproject.payment.model.dto.OrderEmailDTO;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.repository.UserRepository;

@Service
public class OrderDataService {

	 private static final Logger log = LoggerFactory.getLogger(OrderDataService.class);
	    
	    @Autowired
	    private OrderGroupRepository orderGroupRepository;
	    
	    @Autowired
	    private OrdersRepository ordersRepository;
	    
	    @Autowired
	    private UserRepository userRepository;
	    
	    /**
	     * 收集訂單資料用於郵件發送
	     */
	    public OrderEmailDTO collectOrderDataForEmail(Payment payment) {
	        try {
	            log.info("收集訂單資料 - PaymentId: {}, OrderGroupId: {}", 
	                     payment.getId(), payment.getOrderGroupId());
	            
	            // 1. 取得訂單群組
	            OrderGroup orderGroup = orderGroupRepository.findById(payment.getOrderGroupId())
	                    .orElseThrow(() -> new RuntimeException("找不到訂單群組: " + payment.getOrderGroupId()));
	            
	            // 2. 取得用戶資訊
	            User user = userRepository.findById(payment.getPayerUserId())
	                    .orElseThrow(() -> new RuntimeException("找不到用戶: " + payment.getPayerUserId()));
	            
	            // 3. 取得訂單項目
	            List<Orders> orders = ordersRepository.findAllOrdersByGroupId(payment.getOrderGroupId());
	            List<OrderEmailDTO.OrderItemDTO> items = new ArrayList<>();

	            for (Orders order : orders) {
	                for (OrderItems orderItem : order.getOrderItems()) {  // ✅ 改為 getOrderItems()
	                    items.add(convertToOrderItemDTO(orderItem));
	                }
	            }
	            
	            // 4. 計算獲得點數 (基於實付金額)
	            Integer earnedPoints = calculateEarnedPoints(payment.getTotalAmount());
	            
	            // 5. 組裝郵件資料
	            return OrderEmailDTO.builder()
	                    .memberName(user.getNickname() != null ? user.getNickname() : user.getEmail())
	                    .memberEmail(user.getEmail())
	                    .merchantTradeNo(payment.getMerchantTradeNo())
	                    .orderGroupId(payment.getOrderGroupId().toString())
	                    .orderDate(payment.getPaidAt())
	                    .tableInfo(orderGroup.getTable() != null ? 
	                              "桌號 " + orderGroup.getTable().getTableId() : null)
	                    .originalAmount(payment.getTotalAmount() + (payment.getPointsDiscount() != null ? payment.getPointsDiscount() : 0))
	                    .pointsUsed(payment.getPointsUsed())
	                    .pointsDiscount(payment.getPointsDiscount())
	                    .finalAmount(payment.getTotalAmount())
	                    .earnedPoints(earnedPoints)
	                    .items(items)
	                    .build();
	                    
	        } catch (Exception e) {
	            log.error("收集訂單資料失敗 - PaymentId: {}: {}", payment.getId(), e.getMessage(), e);
	            throw new RuntimeException("收集訂單資料失敗", e);
	        }
	    }
	    
	    /**
	     * 轉換訂單項目為 DTO
	     */
	    private OrderEmailDTO.OrderItemDTO convertToOrderItemDTO(com.supernovapos.finalproject.order.model.OrderItems orderItem) {
	        return OrderEmailDTO.OrderItemDTO.builder()
	                .productName(orderItem.getProducts() != null ? orderItem.getProducts().getName() : "商品")
	                .quantity(orderItem.getQuantity())
	                .unitPrice(orderItem.getUnitPrice() != null ? orderItem.getUnitPrice().intValue() : 0)
	                .subtotal(orderItem.getQuantity() * (orderItem.getUnitPrice() != null ? orderItem.getUnitPrice().intValue() : 0))
	                .note(orderItem.getNote())
	                .build();
	    }
	    
	    /**
	     * 計算獲得點數 (每10元1點)
	     */
	    private Integer calculateEarnedPoints(Integer amount) {
	        return amount != null ? amount / 10 : 0;
	    }
	    
	    /**
	     * 檢查是否應該發送郵件
	     */
	    public boolean shouldSendEmail(Payment payment) {
	        try {
	            // 1. 檢查付款是否成功
	            if (!"SUCCESS".equals(payment.getTradeStatus())) {
	                log.debug("付款未成功，不發送郵件 - Status: {}", payment.getTradeStatus());
	                return false;
	            }
	            
	            // 2. 檢查是否有付款人
	            if (payment.getPayerUserId() == null) {
	                log.debug("Guest用戶付款，不發送郵件");
	                return false;
	            }
	            
	            // 3. 檢查用戶是否存在且有 email
	            User user = userRepository.findById(payment.getPayerUserId()).orElse(null);
	            if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
	                log.debug("用戶不存在或無email，不發送郵件");
	                return false;
	            }
	            
	            return true;
	            
	        } catch (Exception e) {
	            log.error("檢查是否發送郵件時發生錯誤: {}", e.getMessage(), e);
	            return false;
	        }
	    }
}
