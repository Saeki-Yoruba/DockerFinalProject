package com.supernovapos.finalproject.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.order.model.OrderGroup;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, UUID> {

//	查詢活躍的訂單群組(用於qr code掃描驗證)
	@Query("select og from OrderGroup og where og.id = :groupId and og.status = true")
	Optional<OrderGroup> findActiveOrderGroup(@Param("groupId") UUID groupId);

//	根據桌號查詢進行中的訂單group
	@Query("select og from OrderGroup og where og.table.id = :tableId and og.status = true")
	Optional<OrderGroup> findActiveOrderGroupByTableId(@Param("tableId") Integer tableId);

//	查詢特定日期的所有完成訂單 (報表用)
	@Query("SELECT og FROM OrderGroup og "
			+ "WHERE og.status = false AND og.completedAt >= :startOfDay AND og.completedAt < :endOfDay")
	List<OrderGroup> findCompletedOrdersByDate(@Param("startOfDay") LocalDateTime startOfDay,
			@Param("endOfDay") LocalDateTime endOfDay);

//	統計今日營業額
	@Query("SELECT COALESCE(SUM(og.totalAmount), 0) FROM OrderGroup og "
			+ "WHERE og.status = false AND og.completedAt >= :startOfDay AND og.completedAt < :endOfDay")
	Integer getTodayTotalRevenue(@Param("startOfDay") LocalDateTime startOfDay,
			@Param("endOfDay") LocalDateTime endOfDay);

//	查詢訂單
	@Query("SELECT og FROM OrderGroup og " +
			"LEFT JOIN FETCH og.orders o " +
			"LEFT JOIN FETCH o.orderItems oi " +
			"LEFT JOIN FETCH oi.products p " +
			"WHERE og.id = :groupId")
	Optional<OrderGroup> findByIdWithDetails(@Param("groupId") UUID groupId);

//	根據ID查詢訂單群組
	Optional<OrderGroup> findById(UUID id);

//	查詢指定餐桌的所有訂單群組（包含已完成）
	@Query("select og from OrderGroup og where og.table.id = :tableId order by og.createdAt desc")
	List<OrderGroup> findAllOrderGroupsByTableId(@Param("tableId") Integer tableId);

//	查詢指定餐桌今日的訂單群組
	@Query("SELECT og FROM OrderGroup og " +
			"WHERE og.table.id = :tableId " +
			"AND og.createdAt >= :startOfDay AND og.createdAt < :endOfDay " +
			"ORDER BY og.createdAt DESC")
	List<OrderGroup> findTodayOrderGroupsByTableId(@Param("tableId") Integer tableId,
			@Param("startOfDay") LocalDateTime startOfDay,
			@Param("endOfDay") LocalDateTime endOfDay);

//	統計各桌今日營業額
	@Query("SELECT og.table.tableId, COALESCE(SUM(og.totalAmount), 0) " +
			"FROM OrderGroup og " +
			"WHERE og.status = false " +
			"AND og.completedAt >= :startOfDay AND og.completedAt < :endOfDay " +
			"GROUP BY og.table.tableId " +
			"ORDER BY og.table.tableId")
	List<Object[]> getTodayRevenueByTable(@Param("startOfDay") LocalDateTime startOfDay,
			@Param("endOfDay") LocalDateTime endOfDay);

//	查詢指定時間範圍內的活躍訂單群組
	@Query("SELECT og FROM OrderGroup og " +
			"WHERE og.status = true " +
			"AND og.createdAt >= :startTime " +
			"ORDER BY og.createdAt DESC")
	List<OrderGroup> findActiveOrderGroupsSince(@Param("startTime") LocalDateTime startTime);

//	統計各時段的訂單數量（用於分析營業高峰）
	@Query("SELECT HOUR(og.createdAt), COUNT(og) " +
			"FROM OrderGroup og " +
			"WHERE og.createdAt >= :startOfDay AND og.createdAt < :endOfDay " +
			"GROUP BY HOUR(og.createdAt) " +
			"ORDER BY HOUR(og.createdAt)")
	List<Object[]> getOrderCountByHour(@Param("startOfDay") LocalDateTime startOfDay,
			@Param("endOfDay") LocalDateTime endOfDay);

	// 查詢活躍的訂單群組並預先加載桌子資訊(用於QR code掃描驗證)
	@Query("SELECT og FROM OrderGroup og JOIN FETCH og.table WHERE og.id = :groupId AND og.status = true")
	Optional<OrderGroup> findActiveOrderGroupWithTable(@Param("groupId") UUID groupId);

}
