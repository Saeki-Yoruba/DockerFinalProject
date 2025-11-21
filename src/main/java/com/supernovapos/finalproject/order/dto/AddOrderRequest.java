package com.supernovapos.finalproject.order.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddOrderRequest {
	
	private List<AddOrderItemRequest> items;
	private String note;

}
