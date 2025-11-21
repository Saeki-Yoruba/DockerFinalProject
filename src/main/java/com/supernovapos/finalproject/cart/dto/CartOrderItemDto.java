package com.supernovapos.finalproject.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartOrderItemDto {
    private Long id;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String note;
    private LocalDateTime createdAt;

    // 保留 products 物件，跟原來一樣
    private ProductSimpleDto products;
}
