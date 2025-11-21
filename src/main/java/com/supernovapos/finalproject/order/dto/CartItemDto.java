package com.supernovapos.finalproject.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.supernovapos.finalproject.order.model.OrderItems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

//統一購物車項目 DTO
public class CartItemDto {
	private Long orderId;                    // 訂單 ID
    private String userType;                 // "TEMP" 或 "REGISTERED"  
    private String userId;                   // 用戶 ID（統一用 String 表示）
    private String userNickname;             // 顯示名稱
    private Integer totalAmount;             // 訂單總額
    private String note;                     // 訂單備註
    private LocalDateTime createdAt;         // 建立時間
    private LocalDateTime submittedAt;       // 提交時間（如果已提交）
    private List<OrderItems> orderItems;    // 訂單項目列表


}
