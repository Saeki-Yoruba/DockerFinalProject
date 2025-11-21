package com.supernovapos.finalproject.product.dto;

import java.util.List;

import com.supernovapos.finalproject.product.model.ProductCategory;
import com.supernovapos.finalproject.product.model.Products;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	分類包含商品 DTO

@Getter 
@Setter
@NoArgsConstructor
public class CategoryWithProductsDto {
	private Integer categoryId;
	private String categoryName;
	private Boolean isActive;
	private List<Products> products;
	private Integer productCount;
	private Integer availableProductCount;
	
}
