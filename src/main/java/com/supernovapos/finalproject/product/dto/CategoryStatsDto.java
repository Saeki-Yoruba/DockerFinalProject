package com.supernovapos.finalproject.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	商品統計DTO

@Getter 
@Setter
@NoArgsConstructor
public class CategoryStatsDto {
	private Integer totalCategories;
	private Integer activeCategories;
	private Integer inactiveCategories;
	
}
