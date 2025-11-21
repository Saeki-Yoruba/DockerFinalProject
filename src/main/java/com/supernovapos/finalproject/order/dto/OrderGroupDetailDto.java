package com.supernovapos.finalproject.order.dto;

import com.supernovapos.finalproject.order.model.OrderGroup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderGroupDetailDto {
	
	private OrderGroup orderGroup;
//    private List<Orders> orders;
    private boolean canSubmitFirstOrder;
    private boolean canAddOrder;
}
