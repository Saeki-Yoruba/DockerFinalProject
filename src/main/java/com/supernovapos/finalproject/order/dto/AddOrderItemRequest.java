package com.supernovapos.finalproject.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddOrderItemRequest {
	
	private Integer productId;
	private Integer quantity;
	private String note;
 }
