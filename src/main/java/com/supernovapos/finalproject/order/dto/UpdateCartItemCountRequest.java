package com.supernovapos.finalproject.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCartItemCountRequest {
	
	private Integer quantity;
	private String note;
	
	public UpdateCartItemCountRequest(Integer quantity, String note) {
        this.quantity = quantity;
        this.note = note;
    }

}
