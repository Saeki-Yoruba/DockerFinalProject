package com.supernovapos.finalproject.product.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	菜單結構 DTO

@Getter 
@Setter
@NoArgsConstructor
public class MenuStructureDto {
	private List<CategoryWithProductsDto> categories;
    private Integer totalCategories;
    private Integer totalProducts;
    private Integer totalAvailableProducts;
	
}
