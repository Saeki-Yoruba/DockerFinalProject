package com.supernovapos.finalproject.product.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 根據分類取得商品統計 DTO
 */
@Getter
@Setter

public class CategoryProductStatsDto {
	private Integer categoryId;
    private int totalProducts;
    private int availableProducts;
    private int unavailableProducts;
}