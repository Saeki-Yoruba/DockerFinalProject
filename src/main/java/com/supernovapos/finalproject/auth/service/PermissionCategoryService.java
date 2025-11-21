package com.supernovapos.finalproject.auth.service;

import java.util.List;

import com.supernovapos.finalproject.auth.model.dto.PermissionCategoryDto;

public interface PermissionCategoryService {

	// 建立分類
	PermissionCategoryDto createCategory(String name, String description);

	// 查詢所有分類
	List<PermissionCategoryDto> getAllCategories();

	// 查詢單一分類
	PermissionCategoryDto getCategory(Integer id);

	// 更新分類
	PermissionCategoryDto updateCategory(Integer id, String name, String description);

	// 刪除分類（要先檢查是否有底下權限）
	void deleteCategory(Integer id);

}