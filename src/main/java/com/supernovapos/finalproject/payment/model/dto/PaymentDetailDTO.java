package com.supernovapos.finalproject.payment.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailDTO {
	
	// 付款查詢結果用
	
	private Long paymentId;
	
	private String merchantTradeNo;
	
	private String tradeNo;
	
	private UUID orderGroupId;
	
	private Long payerUserId;
	
	private Integer totalAmount;
	
	private Integer pointsUsed;
	
	private Integer pointsDiscount;
	
	private String tradeStatus;
	
	private String choosePayment;
	
	private String tradeDesc;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime paidAt;
	
	private String rtnMsg;
}
