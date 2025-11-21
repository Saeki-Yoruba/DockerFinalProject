package com.supernovapos.finalproject.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

//訂單項目統計DTO
public class OrderItemStatsDto {
	private Integer totalItems; // 總商品數量
	private Integer totalAmount; // 總金額
	private Integer itemCount; // 不同商品總數量


}
