package com.supernovapos.finalproject.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	商品統計DTO

@Getter 
@Setter
@NoArgsConstructor
public class ProductStatsDto {
	private Integer totalProducts;
	private Integer availableProducts;
	private Integer unavailableProducts;
	
}
