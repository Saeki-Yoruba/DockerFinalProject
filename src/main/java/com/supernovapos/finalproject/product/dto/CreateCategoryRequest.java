package com.supernovapos.finalproject.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	新增分類請求 DTO

@Getter 
@Setter
@NoArgsConstructor
public class CreateCategoryRequest {
	private String categoryName;
}
