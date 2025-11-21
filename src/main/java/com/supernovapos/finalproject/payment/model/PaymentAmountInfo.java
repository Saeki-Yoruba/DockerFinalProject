package com.supernovapos.finalproject.payment.model;

@lombok.Data
@lombok.Builder
public class PaymentAmountInfo {
	
	private Integer orderTotal;      // 原始總金額
    private Integer pointsDiscount;  // 點數折扣（簡化版為0）
    private Integer actualPayAmount; // 實際付款金額
}

