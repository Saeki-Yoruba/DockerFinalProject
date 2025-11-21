package com.supernovapos.finalproject.payment.model.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEmailDTO {
	
	// 會員資訊
	
	private String memberName;
	
	private String memberEmail;
	
	// 訂單資訊
	
	private String merchantTradeNo;
	
	private String orderGroupId;
	
	private LocalDateTime orderDate;
	
	private String tableInfo;
	
	// 金額資訊
	
	private Integer originalAmount;
	
	private Integer pointsUsed;
	
	private Integer pointsDiscount;
	
	private Integer finalAmount;
	
	private Integer earnedPoints;
	
	// 訂單項目
	
	private List<OrderItemDTO> items;
		
	// 格式化方法
	
	public String getFormattedOrderDate() {
		return orderDate != null ? 
				orderDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")) : "";
	}
	
	public  String getFormattedOriginalAmount() {
		return "NT$ " +( originalAmount != null ? originalAmount.toString() : "0");
	}
	
	public  String getFormattedPointsDiscount() {
		return pointsDiscount != null && pointsDiscount > 0 ? "- NT$" + pointsDiscount : "NT$ 0";
	}
	
	public 	String getFormattedFinalAmount() {
		return "NT$ " + (finalAmount != null ? finalAmount.toString() : "0");
	}
	
	public  Boolean hasPointsUsed() {
		return pointsUsed != null && pointsUsed > 0;
	}
	
	public  Boolean hasEarnedPoints() {
		return earnedPoints != null && earnedPoints > 0;
	}
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OrderItemDTO {
		
		private String productName;
		
        private Integer quantity;
        
        private Integer unitPrice;
        
        private Integer subtotal;
        
        private String note;
        
        public String getFormattedUnitPrice() {
        	return "NT$ " + (unitPrice != null ? unitPrice.toString() : "0");
        }
        
        public String getFormattedSubtotal() {
        	return "NT$ " + (subtotal != null ? subtotal.toString() : "0");
        }
        
        
        
	}
	
	
	
}
