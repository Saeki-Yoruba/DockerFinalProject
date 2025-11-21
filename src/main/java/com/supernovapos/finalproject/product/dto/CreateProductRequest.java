package com.supernovapos.finalproject.product.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	新增商品請求 DTO

@Getter 
@Setter
@NoArgsConstructor
public class CreateProductRequest {
	private String name;
	private BigDecimal price;
	private String description;
	private String image;
	private Integer categoryId;
}
