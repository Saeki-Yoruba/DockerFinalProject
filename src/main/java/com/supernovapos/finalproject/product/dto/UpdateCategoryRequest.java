package com.supernovapos.finalproject.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//	更新分類請求 DTO

@Getter 
@Setter
@NoArgsConstructor
public class UpdateCategoryRequest {
	private String categoryName;
}
