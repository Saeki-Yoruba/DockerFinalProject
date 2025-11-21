package com.supernovapos.finalproject.order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.order.dto.OrderItemStatsDto;
import com.supernovapos.finalproject.order.model.OrderItems;
import com.supernovapos.finalproject.order.repository.OrderItemsRepository;
import com.supernovapos.finalproject.order.repository.OrdersRepository;

//===== 5. OrderItemsService - 訂單項目管理服務 =====

@Service
@Transactional
public class OrderItemsService {
	
	@Autowired
	private OrderItemsRepository orderItemsRepository;
	
	
//	根據訂單 ID 取得所有項目
	public List<OrderItems> getOrderItemsByOrderId(Long orderId){
		return orderItemsRepository.findOrderItemsWithProductsByOrderId(orderId);
	}

//	根據訂單群組 ID 取得所有已提交的項目（廚房顯示用）
	public List<OrderItems> getSubmittedOrderItemsByGroupId(UUID groupId){
		return orderItemsRepository.findSubmittedOrderItemsByGroupId(groupId);
	}
	
//	取得特定日期的商品銷量統計
	public List<Object[]> getProductSalesStatsByDate(LocalDate date){
		return orderItemsRepository.getProductSalesStatsByDate(date);
	}
	
//	取得指定期間的商品銷量統計
	public List<Object[]> getProductSalesStatsBetweenDates(LocalDateTime startDate, LocalDateTime endDate){
		return orderItemsRepository.getProductSalesStatsBetweenDates(startDate, endDate);
	}
	
// 取得特定商品的銷售記錄
	public List<OrderItems> getSoldItemsByProductId(Integer productId){
		return orderItemsRepository.findSoldItemsByProductId(productId);
	}
	
// 	刪除指定訂單的所有項目
	public void deleteOrderItemsByOrderId(Long orderId) {
		orderItemsRepository.deleteByOrderId(orderId);
	}
	
//	計算訂單項目的統計資訊
	public OrderItemStatsDto getOrderItemsStats(Long orderId) {
		List<OrderItems> items = orderItemsRepository.findOrderItemsWithProductsByOrderId(orderId);
		
		Integer totalItems = 0;
		Integer totalAmount = 0;
		
		for(OrderItems item : items) {
			totalItems += item.getQuantity();
			totalAmount += item.getQuantity() * item.getUnitPrice().intValue();
		}
		
		OrderItemStatsDto stats = new OrderItemStatsDto();
		stats.setTotalItems(totalItems);
		stats.setTotalAmount(totalAmount);
		stats.setItemCount(items.size());
		
		return stats;
	}

}
