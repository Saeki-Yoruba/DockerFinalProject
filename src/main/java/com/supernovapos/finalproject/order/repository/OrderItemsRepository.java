package com.supernovapos.finalproject.order.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.order.model.OrderItems;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

//	查詢指定訂單的所有項目
	@Query("select oi from OrderItems oi left join fetch oi.products where oi.orders.id = :orderId")
	List<OrderItems> findOrderItemsWithProductsByOrderId(@Param("orderId") Long orderId);

//	查詢指定訂單群組的所有項目(用於廚房顯示)
	@Query("select oi from OrderItems oi " + "left join fetch oi.products " + "left join fetch oi.orders o "
			+ "where o.orderGroup.id = :groupId and o.status = true " + "order by oi.createdAt ASC")
	List<OrderItems> findSubmittedOrderItemsByGroupId(@Param("groupId") UUID groupId);

// 統計商品銷量(報表用)
	@Query("select oi.products.id, oi.products.name, sum(oi.quantity) as totalQuantity " +
		       "from OrderItems oi " +
		       "join oi.orders o " +
		       "where o.status = true and DATE(oi.createdAt) = :date " +
		       "group by oi.products.id, oi.products.name " +
		       "order by totalQuantity desc")
	List<Object[]> getProductSalesStatsByDate(@Param("date") LocalDate date);

//	刪除指定訂單的所有項目
	@Modifying
	@Query("delete from OrderItems oi where oi.orders.id = :orderId")
	void deleteByOrderId(@Param("orderId") Long orderId);

	// 查詢特定商品的銷售記錄
	@Query("SELECT oi FROM OrderItems oi JOIN oi.orders o WHERE oi.products.id = :productId AND o.status = true")
	List<OrderItems> findSoldItemsByProductId(@Param("productId") Integer productId);

	// 統計特定期間內各商品的銷量
	@Query("SELECT oi.products.id, oi.products.name, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.unitPrice) as totalRevenue "
			+ "FROM OrderItems oi JOIN oi.orders o "
			+ "WHERE o.status = true AND o.createdAt BETWEEN :startDate AND :endDate "
			+ "GROUP BY oi.products.id, oi.products.name " + "ORDER BY totalQuantity DESC")
	List<Object[]> getProductSalesStatsBetweenDates(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

}
