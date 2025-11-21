package com.supernovapos.finalproject.payment.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentItemDTO {
	
	private String productName;     // 商品名稱
	
    private BigDecimal unitPrice;   // 單價
    
    private Integer quantity;       // 數量
    
    private BigDecimal subtotal;    // 小計
    
    private String note;            // 備註（如果有）
    
 // 格式化顯示用
    public String getFormattedUnitPrice() {
        return "NT$ " + unitPrice.intValue();
    }
    
    public String getFormattedSubtotal() {
        return "NT$ " + subtotal.intValue();
    }
}
