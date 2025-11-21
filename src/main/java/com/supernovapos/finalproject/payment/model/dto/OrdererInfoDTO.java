package com.supernovapos.finalproject.payment.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdererInfoDTO {
	
	private String ordererName;     // 點餐人姓名
	
    private List<PaymentItemDTO> items;  // 該人點的商品
    
    private Integer orderTotal;     // 該人的訂單總額
}
