package com.supernovapos.finalproject.cart.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderGroupCartDto {
    private UUID groupId;
    private Integer tableId;
    private List<CartItemDto> cartItems;      
    private List<CartItemDto> submittedOrders; 
    private Integer grandTotal;
    private boolean canSubmitFirstOrder;
    private boolean canAddOrder;
    
    private Integer totalCartAmount;
    private Integer totalSubmittedAmount;
}