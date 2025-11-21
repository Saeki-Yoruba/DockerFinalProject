package com.supernovapos.finalproject.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.order.model.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

//	查詢指定訂單群組的所有草稿訂單(購物車內容)
	@Query("select o from Orders o where o.orderGroup.id = :groupId and o.status = false")
	List<Orders> findDraftOrdersByGroupId(@Param("groupId") UUID groupId);

//	查詢指定訂單群組的所有已提交訂單
	@Query("select o from Orders o where o.orderGroup.id = :groupId and o.status = true")
	List<Orders> findSubmittedOrdersByGroupId(@Param("groupId") UUID groupId);

//	查詢指定訂單群組的所有訂單
	@Query("select o from Orders o where o.orderGroup.id = :groupId order by o.createdAt ASC")
	List<Orders> findAllOrdersByGroupId(@Param("groupId") UUID groupId);

//	查詢臨時用戶在指定訂單群組的購物車
	@Query("select o from Orders o where o.orderGroup.id = :groupId and o.tempUser.id = :tempUserId and o.status = false")
	Optional<Orders> findTempUserDraftOrder(@Param("groupId") UUID groupId, @Param("tempUserId") UUID tempUserId);

//	查詢註冊用戶在指定訂單群組的購物車
	@Query("select o from Orders o where o.orderGroup.id = :groupId and o.user.id = :userId and o.status = false")
	Optional<Orders> findRegisteredUserDraftOrder(@Param("groupId") UUID groupId, @Param("userId") Long userId);

//	檢查訂單群組是否有已提交的訂單
	@Query("select count(o) > 0 from Orders o where o.orderGroup.id = :groupId and o.status = true")
	boolean existsSubmittedOrderByGroupId(@Param("groupId") UUID groupId);

//	計算訂單群組的總金額
	@Query("select coalesce(sum(o.totalAmount),0) from Orders o where o.orderGroup.id = :groupId and o.status = true")
	Integer sumTotalAmountByGroupId(@Param("groupId") UUID groupId);

//	批量更新草稿訂單為已提交
	@Modifying
	@Query("update Orders o set o.status = true, o.updatedAt = CURRENT_TIMESTAMP where o.orderGroup.id = :groupId and o.status = false")
	int updateDraftOrdersToSubmitted(@Param("groupId") UUID groupId);

	// 查詢臨時用戶的所有訂單（包含訂單項目）
	@Query("SELECT o FROM Orders o LEFT JOIN FETCH o.orderItems WHERE o.tempUser.id = :tempUserId")
	List<Orders> findOrdersWithItemsByTempUserId(@Param("tempUserId") UUID tempUserId);

	// 查詢註冊用戶的所有訂單（包含訂單項目）
	@Query("SELECT o FROM Orders o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId")
	List<Orders> findOrdersWithItemsByRegisteredUserId(@Param("userId") Long userId);

	// 查詢特定時間範圍的訂單（報表用）
	@Query("SELECT o FROM Orders o WHERE o.status = true AND o.createdAt BETWEEN :startDate AND :endDate")
	List<Orders> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	// OrdersRepository - 加上常用的組合查詢
	@Query("SELECT o FROM Orders o WHERE o.orderGroup.id = :groupId AND o.status = :status ORDER BY o.createdAt DESC")
	List<Orders> findOrdersByGroupIdAndStatus(@Param("groupId") UUID groupId, @Param("status") Boolean status);

	// 查詢臨時用戶的購物車（包含所有相關資料）
	@Query("SELECT o FROM Orders o " +
			"LEFT JOIN FETCH o.orderItems oi " +
			"LEFT JOIN FETCH oi.products p " +
			"LEFT JOIN FETCH p.productCategory " +
			"LEFT JOIN FETCH o.tempUser " +
			"WHERE o.orderGroup.id = :groupId AND o.tempUser.id = :tempUserId AND o.status = false")
	Optional<Orders> findTempUserCartWithAllData(@Param("groupId") UUID groupId, @Param("tempUserId") UUID tempUserId);

	// 查詢註冊用戶的購物車（包含所有相關資料）
	@Query("SELECT o FROM Orders o " +
			"LEFT JOIN FETCH o.orderItems oi " +
			"LEFT JOIN FETCH oi.products p " +
			"LEFT JOIN FETCH p.productCategory " +
			"LEFT JOIN FETCH o.user " +
			"WHERE o.orderGroup.id = :groupId AND o.user.id = :userId AND o.status = false")
	Optional<Orders> findRegisteredUserCartWithAllData(@Param("groupId") UUID groupId, @Param("userId") Long userId);
	
	// 查詢指定訂單群組的所有訂單（包含完整的相關資料）
	@Query("SELECT DISTINCT o FROM Orders o " +
	       "LEFT JOIN FETCH o.orderItems oi " +
	       "LEFT JOIN FETCH oi.products p " +
	       "LEFT JOIN FETCH p.productCategory " +
	       "LEFT JOIN FETCH o.tempUser " +
	       "LEFT JOIN FETCH o.user " +
	       "WHERE o.orderGroup.id = :groupId " +
	       "ORDER BY o.createdAt ASC")
	List<Orders> findAllOrdersByGroupIdWithDetails(@Param("groupId") UUID groupId);

}
