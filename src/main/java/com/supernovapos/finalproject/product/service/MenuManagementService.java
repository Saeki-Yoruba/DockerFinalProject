package com.supernovapos.finalproject.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.product.dto.BatchUpdateProductStatusRequest;
import com.supernovapos.finalproject.product.dto.CategoryWithProductsDto;
import com.supernovapos.finalproject.product.dto.MenuStructureDto;
import com.supernovapos.finalproject.product.model.ProductCategory;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.repository.ProductCategoryRepository;
import com.supernovapos.finalproject.product.repository.ProductsRepository;

//===== 3. MenuManagementService - 菜單管理服務 =====
@Service
@Transactional
public class MenuManagementService {

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@Autowired
	private ProductsRepository productsRepository;

//	取得完整菜單結構(管理端用)
	public MenuStructureDto getFullMenuStructure() {
		List<ProductCategory> allCategories = productCategoryRepository.findAllOrderByName();
		List<CategoryWithProductsDto> categoryList = new ArrayList<CategoryWithProductsDto>();

		for (ProductCategory category : allCategories) {
			List<Products> products = productsRepository.findByCategoryId(category.getId());

			CategoryWithProductsDto categoryDto = new CategoryWithProductsDto();
			categoryDto.setProducts(products);
			categoryDto.setProductCount(products.size());

			int availableCount = 0;
			for (Products product : products) {
				if (product.getIsAvailable()) {
					availableCount++;
				}
			}
			categoryDto.setAvailableProductCount(availableCount);
			categoryList.add(categoryDto);
		}

		MenuStructureDto menuStructure = new MenuStructureDto();
		menuStructure.setCategories(categoryList);
		menuStructure.setTotalCategories(categoryList.size());

		int totalProducts = 0;
		int totalAvailableProducts = 0;
		for (CategoryWithProductsDto categoryDto : categoryList) {
			totalProducts += categoryDto.getProductCount();
			totalAvailableProducts += categoryDto.getAvailableProductCount();
		}

		menuStructure.setTotalProducts(totalProducts);
		menuStructure.setTotalAvailableProducts(totalAvailableProducts);

		return menuStructure;
	}

//	取得客戶端菜單結構
	public MenuStructureDto getCustomerMenuStructure() {
		List<ProductCategory> activeCategories = productCategoryRepository.findAllActiveCategories();
		List<CategoryWithProductsDto> categoryList = new ArrayList<CategoryWithProductsDto>();

		for (ProductCategory category : activeCategories) {
			List<Products> availableProducts = productsRepository
					.findAllAvailableProductsByCategoryId(category.getId());

			if (!availableProducts.isEmpty()) {
				CategoryWithProductsDto categoryDto = new CategoryWithProductsDto();
				categoryDto.setCategoryId(category.getId());
				categoryDto.setCategoryName(category.getCategoryName());
				categoryDto.setIsActive(category.getIsActive());
				
				categoryDto.setProducts(availableProducts);
				categoryDto.setProductCount(availableProducts.size());
				categoryDto.setAvailableProductCount(availableProducts.size());

				categoryList.add(categoryDto);
			}
		}

		MenuStructureDto menuStructure = new MenuStructureDto();
		menuStructure.setCategories(categoryList);
		menuStructure.setTotalCategories(categoryList.size());

		int totalProducts = 0;
		for (CategoryWithProductsDto categoryDto : categoryList) {
			totalProducts += categoryDto.getProductCount();
		}
		
		menuStructure.setTotalProducts(totalProducts);
		menuStructure.setTotalAvailableProducts(totalProducts);
		
		return menuStructure;
	}
	
//	批量更新商品狀態
	public Integer batchUpdateProductStatus(BatchUpdateProductStatusRequest request) {
		return productsRepository.batchUpdateProductStatus(request.getProductIds(), request.getIsAvailable());
	}
	
//	清理空分類
	public Integer cleanupEmptyCategories() {
		List<ProductCategory> emptyCategories = productCategoryRepository.findEmptyCategories();
		
		for(ProductCategory category : emptyCategories) {
			productCategoryRepository.delete(category);
		}
		return emptyCategories.size();
	}
}
