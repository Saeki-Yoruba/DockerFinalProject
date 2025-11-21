package com.supernovapos.finalproject.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderItemDto {
	private String productName;
    private Integer quantity;
    private Integer unitPrice;
}
