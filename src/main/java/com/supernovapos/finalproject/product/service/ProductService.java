package com.supernovapos.finalproject.product.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.product.model.ProductCategory;
import com.supernovapos.finalproject.product.model.Products;
import com.supernovapos.finalproject.product.repository.ProductCategoryRepository;
import com.supernovapos.finalproject.product.repository.ProductsRepository;

@Service
@Transactional
public class ProductService {
	
	@Autowired
	private ProductsRepository productsRepository;
	
	@Autowired
	private ProductCategoryRepository productCategoryRepository;
	
//	取得所有可用商品
	public List<Products> getAllAvailableProducts(){
		return productsRepository.findAllAvailableProducts();
	}
	
//	根據分類取得可用商品
	public List<Products> getAvailableProductsByCategory(Integer categoryId){
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		if(!categoryOpt.isPresent()) {
			throw new ResourceNotFoundException("商品分類不存在");
		}
		
		return productsRepository.findAllAvailableProductsByCategoryId(categoryId);
	}
	
//	搜尋商品
	public List<Products> searchProducts(String keyword){
		if(keyword == null || keyword.trim().isEmpty()) {
			return getAllAvailableProducts();
		}
		
		return productsRepository.searchAvailableProducts(keyword);
	}
	
//	取得熱門商品
	public List<Products> getPopularProducts(Pageable pageable){
		return productsRepository.findPopularProducts(pageable);
	}
	
//	分頁查詢商品
	public Page<Products> getProductsWithPagination(Integer categoryId, Pageable pageable){
		return productsRepository.findAvailableProductsWithOptionalCategory(categoryId, pageable);
	}
	
//	根據ID取得商品
	public Products getProductById(Integer productId) {
		Optional<Products> productOpt = productsRepository.findById(productId);
		if(!productOpt.isPresent()) {
			throw new ResourceNotFoundException("商品不存在");
		}
		return productOpt.get();
	}
	
//	取得所有啟用的商品分類
	public List<ProductCategory> getAllActiveCategories(){
		return productCategoryRepository.findAllActiveCategories();
	}
	
//	取得所有商品分類
	public List<ProductCategory> getCategoriesWithProducts(){
		return productCategoryRepository.findCategoriesWithAvailableProducts();
	}
	
//	根據 ID 取得商品分類
	public ProductCategory getcategoryById(Integer categoryId) {
		 Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(categoryId);
		 if(!categoryOpt.isPresent()) {
			 throw new ResourceNotFoundException("商品分類不存在");
		 }
		 return categoryOpt.get();
	}
	
//	根據名稱取得商品分類
	public ProductCategory getCategoryByName(String categoryName) {
		Optional<ProductCategory> categoryOpt = productCategoryRepository.findByCategoryNameAndIsActiveTrue(categoryName);
		if (!categoryOpt.isPresent()) {
            throw new ResourceNotFoundException("找不到名稱為 '" + categoryName + "' 的商品分類");
        }
        return categoryOpt.get();
	}
	
}
