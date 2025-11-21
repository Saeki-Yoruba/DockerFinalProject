package com.supernovapos.finalproject.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TablePaymentRequest {
	
	private String table;
    private Integer total;

}
