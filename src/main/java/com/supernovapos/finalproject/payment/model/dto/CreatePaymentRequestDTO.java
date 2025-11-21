package com.supernovapos.finalproject.payment.model.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequestDTO {

	// 建立付款請求用
	private UUID orderGroupId;

	private Long payerUserId;

	// 要使用的點數
	private Integer pointsToUse;

	private String returnURL;

	// 驗證方法

	public void validate() {
		if (orderGroupId == null) {
			throw new IllegalArgumentException("訂單群組ID不能為空");
		}
//		if(payerUserId == null) {
//			throw new IllegalArgumentException("付款人ID不能為空");
//		}
		if (pointsToUse != null && pointsToUse < 0) {
			throw new IllegalArgumentException("使用點數不能為負數");
		}
		if (payerUserId == null && pointsToUse != null && pointsToUse > 0) {
			throw new IllegalArgumentException("訪客無法使用點數折抵");
		}
	}

	public boolean isUsingPoints() {
		return pointsToUse != null && pointsToUse > 0 && payerUserId != null;
	}

	public Integer getSafePointsToUse() {
		return (payerUserId == null) ? 0 : (pointsToUse != null ? pointsToUse : 0);
	}

	public boolean isGuest() {
		return payerUserId == null;
	}
}
