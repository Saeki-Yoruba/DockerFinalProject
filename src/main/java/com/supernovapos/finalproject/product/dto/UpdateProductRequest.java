package com.supernovapos.finalproject.product.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	更新商品請求 DTO

@Getter 
@Setter
@NoArgsConstructor
public class UpdateProductRequest {
	private String name;
	private BigDecimal price;
	private String description;
	private String image;
	private Integer categoryId;
}
