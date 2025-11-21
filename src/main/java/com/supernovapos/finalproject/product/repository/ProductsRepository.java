package com.supernovapos.finalproject.product.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.product.model.Products;

public interface ProductsRepository extends JpaRepository<Products, Integer> {
	
//	查詢所有可用商品
	@Query("select p from Products p where p.isAvailable = true order by p.name asc")
	List<Products> findAllAvailableProducts();
	
//	根據分類查詢可用商品
	@Query("select p from Products p where p.productCategory.id = :categoryId and p.isAvailable = true order by p.name asc")
	List<Products> findAllAvailableProductsByCategoryId(@Param("categoryId") Integer categoryId);
	
//	搜尋商品(按名稱)
	@Query("select p from Products p where p.isAvailable = true and lower(p.name) like lower(concat('%', :keyword, '%')) order by p.name asc")
	List<Products> searchAvailableProducts(@Param("keyword") String keyword);

// 	查詢熱門商品
	@Query("select p from Products p " +
	           "left join OrderItems oi on p.id = oi.products.id " +
	           "left join Orders o on oi.orders.id = o.id " +
	           "where p.isAvailable = true and o.status = true " +
	           "group by p.id, p.name, p.price, p.isAvailable, p.image, p.description, " +
	           "p.createdAt, p.updatedAt, p.productCategory " +
	           "order by sum(oi.quantity) desc")
	List<Products> findPopularProducts(Pageable pageable);
	
	@Query("SELECT p FROM Products p WHERE p.isAvailable = true AND " +
		       "(:categoryId IS NULL OR p.productCategory.id = :categoryId) " +
		       "ORDER BY p.name ASC")
	Page<Products> findAvailableProductsWithOptionalCategory(@Param("categoryId") Integer categoryId, Pageable pageable);

// === 管理端新增方法 ===
    
	/**
	 * 查詢所有商品並包含完整的分類資訊（管理端用）
	 */
	@Query("SELECT p FROM Products p " +
	       "LEFT JOIN FETCH p.productCategory pc " +
	       "ORDER BY p.createdAt DESC")
	List<Products> findAllProductsWithCategory();

	
    /**
     * 根據分類查詢所有商品（包含已下架）- 管理端用
     */
    @Query("select p from Products p where p.productCategory.id = :categoryId order by p.name asc")
    List<Products> findByCategoryId(@Param("categoryId") Integer categoryId);
    
    /**
     * 搜尋所有商品（包含已下架）- 管理端用
     */
    @Query("select p from Products p where lower(p.name) like lower(concat('%', :keyword, '%')) order by p.name asc")
    List<Products> searchAllProducts(@Param("keyword") String keyword);
    
    /**
     * 統計指定分類的商品數量
     */
    @Query("select count(p) from Products p where p.productCategory.id = :categoryId")
    int countByCategoryId(@Param("categoryId") Integer categoryId);
    
    /**
     * 統計指定分類的可用商品數量
     */
    @Query("select count(p) from Products p where p.productCategory.id = :categoryId and p.isAvailable = true")
    int countAvailableByCategoryId(@Param("categoryId") Integer categoryId);
    
    /**
     * 批量更新商品狀態
     */
    @Modifying
    @Query("update Products p set p.isAvailable = :isAvailable where p.id in :productIds")
    int batchUpdateProductStatus(@Param("productIds") List<Integer> productIds, @Param("isAvailable") Boolean isAvailable);
}
