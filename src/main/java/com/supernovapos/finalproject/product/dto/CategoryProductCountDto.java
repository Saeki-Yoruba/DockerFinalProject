package com.supernovapos.finalproject.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 分類商品統計 DTO
 */
@Getter
@Setter
@NoArgsConstructor

public class CategoryProductCountDto {
    private Integer categoryId;
    private String categoryName;
    private int totalProducts;
    private int availableProducts;
    private int unavailableProducts;
    
    }
