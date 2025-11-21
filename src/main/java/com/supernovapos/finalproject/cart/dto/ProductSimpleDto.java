package com.supernovapos.finalproject.cart.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSimpleDto {
    private Integer id;
    private String name;
    private String image;
    private BigDecimal price;
}
