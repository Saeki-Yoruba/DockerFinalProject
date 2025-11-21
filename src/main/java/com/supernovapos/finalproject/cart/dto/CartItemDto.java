package com.supernovapos.finalproject.cart.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Long orderId;
    private String userId;
    private String userType;
    private String userNickname;
    private List<CartOrderItemDto> orderItems;
    private Integer totalAmount;
	private String note;
	private LocalDateTime createdAt;
}
