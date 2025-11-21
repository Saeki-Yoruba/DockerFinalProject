package com.supernovapos.finalproject.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.product.dto.CategoryProductCountDto;
import com.supernovapos.finalproject.product.dto.CategoryStatsDto;
import com.supernovapos.finalproject.product.dto.CreateCategoryRequest;
import com.supernovapos.finalproject.product.dto.UpdateCategoryRequest;
import com.supernovapos.finalproject.product.model.ProductCategory;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.repository.ProductCategoryRepository;
import com.supernovapos.finalproject.product.repository.ProductsRepository;

//===== 2. CategoryManagementService - 分類管理服務 =====
@Service
@Transactional
public class CategoryManagementService {

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@Autowired
	private ProductsRepository productsRepository;

//   新增商品分類
	public ProductCategory createCategory(CreateCategoryRequest request) {
		Optional<ProductCategory> existingCategory = productCategoryRepository
				.findByCategoryName(request.getCategoryName());
		if (existingCategory.isPresent()) {
			throw new ConflictException("分類名稱 '" + request.getCategoryName() + "' 已存在");
		}

		ProductCategory category = new ProductCategory();
		category.setCategoryName(request.getCategoryName());
		category.setIsActive(true);

		return productCategoryRepository.save(category);
	}

//	更新分類資訊
	public ProductCategory updateCategory(Integer categoryId, UpdateCategoryRequest request) {
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		// 檢查商品分類是否存在
		if (!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}
		ProductCategory category = categoryOpt.get();

		// 檢查分類名稱是否存在
		if (request.getCategoryName() != null) {
			Optional<ProductCategory> existingCategory = productCategoryRepository
					.findByCategoryName(request.getCategoryName());
			if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
				throw new ConflictException("分類名稱 '" + request.getCategoryName() + "' 已存在");
			}
			category.setCategoryName(request.getCategoryName());
		}
		return productCategoryRepository.save(category);
	}

//	刪除分類
	public void deleteCategory(Integer categoryId) {
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		if (!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}

		List<Products> productsInCategory = productsRepository.findByCategoryId(categoryId);
		if (!productsInCategory.isEmpty()) {
			throw new ConflictException("無法刪除分類，因為還有 " + productsInCategory.size() + " 個商品使用此分類");
		}
		productCategoryRepository.deleteById(categoryId);
	}

//	強制刪除分類(同時刪除分類底下的商品)
	public void forceDeleteCategory(Integer categoryId) {
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		if (!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}
		List<Products> productsInCategory = productsRepository.findByCategoryId(categoryId);
		for (Products product : productsInCategory) {
			productsRepository.delete(product);
		}

		productCategoryRepository.deleteById(categoryId);
	}

//	啟用分類
	public void enableCategory(Integer categoryId) {
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		if (!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}

		ProductCategory category = categoryOpt.get();
		category.setIsActive(true);
		productCategoryRepository.save(category);
	}

//	停用分類
	public void disableCategory(Integer categoryId) {
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		if (!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}

		ProductCategory category = categoryOpt.get();
		category.setIsActive(false);

		// 分類底下商品也停用(是否需要？)
		List<Products> productsInCategory = productsRepository.findByCategoryId(categoryId);
		for (Products product : productsInCategory) {
			product.setIsAvailable(false);
			productsRepository.save(product);
		}

		productCategoryRepository.save(category);
	}

//	取得所有分類(含已停用)
	public List<ProductCategory> getAllCategory() {
		return productCategoryRepository.findAllOrderByName();
	}

//	取得有商品的分類（包含已停用分類和已下架商品）(管理端)
	public List<ProductCategory> getCategoriesWithProducts() {
		return productCategoryRepository.findCategoriesWithProducts();
	}

//	取得空分類
	public List<ProductCategory> getEmptyCategory() {
		return productCategoryRepository.findEmptyCategories();
	}

//	取得分類統計資訊
	public CategoryStatsDto getCategoryStats() {
		List<ProductCategory> allCategories = getAllCategory();

		Integer totalCategories = allCategories.size();
		Integer activeCategories = 0;
		Integer inactiveCategories = 0;

		for (ProductCategory category : allCategories) {
			if (category.getIsActive()) {
				activeCategories++;
			} else {
				inactiveCategories++;
			}
		}

		CategoryStatsDto stats = new CategoryStatsDto();
		stats.setTotalCategories(totalCategories);
		stats.setActiveCategories(activeCategories);
		stats.setInactiveCategories(inactiveCategories);

		return stats;
	}

//	取得分類底下商品數量
	public List<CategoryProductCountDto> getCategoryProductCounts() {
		List<Object[]> results = productCategoryRepository.getCategoryProductCounts();
		ArrayList<CategoryProductCountDto> stats = new ArrayList<CategoryProductCountDto>();

		for (Object[] result : results) {
			Integer categoryId = (Integer) result[0];
			String categoryName = (String) result[1];
			Long productCount = (Long) result[2];

			int availableCount = productsRepository.countAvailableByCategoryId(categoryId);

			CategoryProductCountDto dto = new CategoryProductCountDto();
			dto.setCategoryId(categoryId);
			dto.setCategoryName(categoryName);
			dto.setTotalProducts(productCount.intValue());
			dto.setAvailableProducts(availableCount);
			dto.setUnavailableProducts(productCount.intValue() - availableCount);

			stats.add(dto);
		}
		return stats;
	}

//	移動商品到其他分類

	public void moveProductsToCategory(Integer formCategoryId, Integer toCategoryId) {
		Optional<ProductCategory> fromCategoryOpt = productCategoryRepository.findById(formCategoryId);
		Optional<ProductCategory> toCategoryOpt = productCategoryRepository.findById(toCategoryId);

		if (!fromCategoryOpt.isPresent() || !toCategoryOpt.isPresent()) {
			throw new ResourceNotFoundException("來源分類或目標分類不存在");
		}

		ProductCategory toCategory = toCategoryOpt.get();
		if (!toCategory.getIsActive()) {
			throw new InvalidRequestException("無法移動商品到已停用的分類");
		}

		List<Products> productsToMove = productsRepository.findByCategoryId(formCategoryId);
		for (Products product : productsToMove) {
			product.setProductCategory(toCategory);
			productsRepository.save(product);
		}
	}
}
