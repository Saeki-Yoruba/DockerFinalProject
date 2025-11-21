package com.supernovapos.finalproject.payment.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultDTO {
	
	//付款結果用
	
	private String merchantTradeNo;
	
	private String tradeStatus;
 
	private boolean success;
 
	private String message;
 
	private Integer totalAmount;
 
	private LocalDateTime paidAt;
}
