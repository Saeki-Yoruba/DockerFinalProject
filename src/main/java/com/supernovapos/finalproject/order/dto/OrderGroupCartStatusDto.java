package com.supernovapos.finalproject.order.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderGroupCartStatusDto {
	
	private UUID groupId;                          // 訂單組 ID
	private Integer tableId; 
    private List<CartItemDto> cartItems = new ArrayList<>();           // 購物車內容（草稿狀態）
    private List<CartItemDto> submittedOrders = new ArrayList<>();     // 已提交訂單
    private Integer totalCartAmount = 0;               // 購物車總額
    private Integer totalSubmittedAmount = 0;          // 已提交訂單總額
    private Integer grandTotal = 0;                    // 總計金額
    private boolean canSubmitFirstOrder = true;           // 是否可提交首次訂單
    private boolean canAddOrder = false;                   // 是否可加點
}
