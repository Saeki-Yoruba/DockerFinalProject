package com.supernovapos.finalproject.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.product.model.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

//	查詢所有啟用分類
	@Query("select pc from ProductCategory pc where pc.isActive = true order by pc.categoryName asc")
	List<ProductCategory> findAllActiveCategories();

//	根據分類名稱查詢
	@Query("select pc from ProductCategory pc where pc.isActive = true and lower(pc.categoryName) like lower(concat('%', :categoryName, '%'))")
	Optional<ProductCategory> findByCategoryNameAndIsActiveTrue(@Param("categoryName") String categoryName);

//	查詢有商品的分類
	@Query("select distinct pc from ProductCategory pc " +
			"join pc.products p " +
			"where pc.isActive = true and p.isAvailable = true " +
			"order by pc.categoryName asc")
	List<ProductCategory> findCategoriesWithAvailableProducts();

// === 管理端新增方法 ===

	/**
	 * 根據分類名稱查詢（不限制 isActive 狀態）- 管理端用
	 */
	@Query("select pc from ProductCategory pc where lower(pc.categoryName) like lower(concat('%', :categoryName, '%'))")
	Optional<ProductCategory> findByCategoryName(@Param("categoryName") String categoryName);

	/**
	 * 查詢所有分類並按名稱排序（包含已停用）- 管理端用
	 */
	@Query("select pc from ProductCategory pc order by pc.categoryName asc")
	List<ProductCategory> findAllOrderByName();

	/**
	 * 查詢有商品的分類（包含已停用分類和已下架商品）- 管理端用
	 */
	@Query("select distinct pc from ProductCategory pc " +
			"join pc.products p " +
			"order by pc.categoryName asc")
	List<ProductCategory> findCategoriesWithProducts();

	/**
	 * 查詢空分類（沒有任何商品）
	 */
	@Query("select pc from ProductCategory pc " +
			"where pc.id not in (select distinct p.productCategory.id from Products p) " +
			"order by pc.categoryName asc")
	List<ProductCategory> findEmptyCategories();

	/**
	 * 統計各分類的商品數量
	 */
	@Query("select pc.id, pc.categoryName, count(p) " +
			"from ProductCategory pc left join pc.products p " +
			"group by pc.id, pc.categoryName " +
			"order by pc.categoryName asc")
	List<Object[]> getCategoryProductCounts();

}
