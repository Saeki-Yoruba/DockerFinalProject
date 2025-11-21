package com.supernovapos.finalproject.product.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.product.dto.CategoryProductStatsDto;
import com.supernovapos.finalproject.product.dto.CreateProductRequest;
import com.supernovapos.finalproject.product.dto.ProductStatsDto;
import com.supernovapos.finalproject.product.dto.UpdateProductRequest;
import com.supernovapos.finalproject.product.model.ProductCategory;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.repository.ProductCategoryRepository;
import com.supernovapos.finalproject.product.repository.ProductsRepository;

//===== 1. ProductManagementService - 商品管理服務 =====
@Service
@Transactional
public class ProductManagementService {

	@Autowired
	private ProductsRepository productsRepository;

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

//	新增商品
	public Products createProduct(CreateProductRequest request) {
		// 檢查分類是否存在
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(request.getCategoryId());
		if (!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}
		ProductCategory category = categoryOpt.get();
		// 檢查分類是否啟用
		if (!category.getIsActive()) {
			throw new InvalidRequestException("無法在已停用的分類中新增商品");
		}

		Products product = new Products();
		product.setName(request.getName());
		product.setPrice(request.getPrice());
		product.setDescription(request.getDescription());
		product.setImage(request.getImage());
		product.setIsAvailable(true); // 預設為可用
		product.setProductCategory(category);

		return productsRepository.save(product);
	}

//	更新商品資訊
	public Products updateProduct(Integer productId, UpdateProductRequest request) {
		Optional<Products> productOpt = productsRepository.findById(productId);
		if (!productOpt.isPresent()) {
			throw new ResourceNotFoundException("商品不存在");
		}
		Products product = productOpt.get();

		// 更新基本資訊
		if (request.getName() != null) {
			product.setName(request.getName());
		}
		if (request.getPrice() != null) {
			product.setPrice(request.getPrice());
		}
		if (request.getDescription() != null) {
			product.setDescription(request.getDescription());
		}
		if (request.getImage() != null) {
			product.setImage(request.getImage());
		}

		// 更新分類
		if (request.getCategoryId() != null) {
			Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(request.getCategoryId());
			if (!categoryOpt.isPresent()) {
				throw new ResourceNotFoundException("商品分類不存在");
			}
			ProductCategory category = categoryOpt.get();

			if (!category.getIsActive()) {
				throw new InvalidRequestException("無法將商品移至已停用的分類");
			}
			product.setProductCategory(category);
		}
		return productsRepository.save(product);
	}

	/**
	 * 批量更新商品狀態（使用 Repository 的批量方法）
	 */
	public int batchUpdateProductStatus(List<Integer> productIds, Boolean isAvailable) {
		return productsRepository.batchUpdateProductStatus(productIds, isAvailable);
	}

//	刪除商品
	public void deleteProduct(Integer productId) {
		Optional<Products> productOpt = productsRepository.findById(productId);
		if (!productOpt.isPresent()) {
			throw new ResourceNotFoundException("商品不存在");
		}

		productsRepository.deleteById(productId);
	}

//	下架商品
	public void disableProduct(Integer productId) {
		Optional<Products> productOpt = productsRepository.findById(productId);
		if (!productOpt.isPresent()) {
			throw new ResourceNotFoundException("商品不存在");
		}

		// 軟刪除：設為不可用而不是真的刪除
		Products product = productOpt.get();
		product.setIsAvailable(false);
		productsRepository.save(product);
	}

//	批量下架商品
	public void disableProducts(List<Integer> productIds) {
		for (Integer productId : productIds) {
			disableProduct(productId);
		}
	}

//	上架商品
	public void enableProduct(Integer productId) {
		Optional<Products> productOpt = productsRepository.findById(productId);
		if (!productOpt.isPresent()) {
			throw new ResourceNotFoundException("商品不存在");
		}
		Products product = productOpt.get();

		// 檢查分類是否啟用
		if (!product.getProductCategory().getIsActive()) {
			throw new InvalidRequestException("無法上架已停用分類中的商品");
		}

		product.setIsAvailable(true);
		productsRepository.save(product);
	}

//	批量上架商品
	public void enableProducts(List<Integer> productIds) {
		for (Integer productId : productIds) {
			enableProduct(productId);
		}
	}

//	取得所有商品（包含已下架）

	public List<Products> getAllProducts() {
		return productsRepository.findAllProductsWithCategory();
	}

//	分頁取得所有商品(包含已下架）- 管理端用
	public Page<Products> getAllProductsWithPagination(Pageable pageable) {
		return productsRepository.findAll(pageable);
	}

//	根據分類取得商品（包含已下架）- 管理端用
	public List<Products> getProductsByCategory(Integer categoryId) {
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		if (!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}

		return productsRepository.findByCategoryId(categoryId);
	}

//  搜尋商品（包含已下架) - 管理端用
	public List<Products> searchAllProducts(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return getAllProducts();
		}

		return productsRepository.searchAllProducts(keyword.trim());
	}

//  取得商品統計資訊 - 管理端用   
	public ProductStatsDto getProductStats() {
		List<Products> allProducts = getAllProducts();

		Integer totalProducts = allProducts.size();
		Integer availableProducts = 0;
		Integer unavailableProducts = 0;

		for (Products product : allProducts) {
			if (product.getIsAvailable()) {
				availableProducts++;
			} else {
				unavailableProducts++;
			}
		}
		
		ProductStatsDto stats = new ProductStatsDto();
		stats.setTotalProducts(totalProducts);
		stats.setAvailableProducts(availableProducts);
		stats.setUnavailableProducts(unavailableProducts);
		
		return stats;
	}
	
//  根據分類取得商品統計 - 管理端用 
	public CategoryProductStatsDto getProductStatsByCategory(Integer categoryId) {
		Integer totalCount = productsRepository.countByCategoryId(categoryId);
        Integer availableCount = productsRepository.countAvailableByCategoryId(categoryId);
        
        CategoryProductStatsDto stats = new CategoryProductStatsDto();
        stats.setCategoryId(categoryId);
        stats.setTotalProducts(totalCount);
        stats.setAvailableProducts(availableCount);
        stats.setUnavailableProducts(totalCount - availableCount);
        
        return stats;
    
	}

}
