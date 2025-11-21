package com.supernovapos.finalproject.payment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsDTO {
	/**
	 * 數量 (來自 order_items 表)
	 */
	private Integer quantity;

	/**
	 * 餐點名稱 (來自 products 表)
	 */
	private String name;

	/**
	 * 單價 (來自 order_items 表)
	 */
	private Long unitPrice;

	/**
	 * 整桌總金額 (來自 order_groups 表)
	 */
	private Integer totalAmount;

	/**
	 * 對應的桌子 (來自 order_groups 表)
	 */
	private Integer tableId;

	/**
	 * 點數異動量 (來自 points 表)
	 */
	private Integer pointsAmount;

	/**
	 * 選擇付款方式 (來自 payments 表)
	 */
	private String choosePayment;

	/**
	 * 下單人 (來自 orders 表)
	 */
	private String customerName;
}
