package com.supernovapos.finalproject.table.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.table.model.RestaurantTable;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {

	boolean existsByTableId(Integer tableId);

//	Roy新增
	// 根據桌號查詢餐桌
	Optional<RestaurantTable> findByTableId(Integer tableId);
	// 查詢所有可用桌子
		@Query("SELECT rt FROM RestaurantTable rt WHERE rt.isAvailable = '空桌'")
		List<RestaurantTable> findAllAvailableTables();
		
		// 查詢所有用餐中桌子
		@Query("SELECT rt FROM RestaurantTable rt WHERE rt.isAvailable = '用餐'")
		List<RestaurantTable> findAllDiningTables();
		
		// 查詢指定容量以上的可用桌子
		@Query("SELECT rt FROM RestaurantTable rt WHERE rt.capacity >= :minCapacity AND rt.isAvailable = '空桌' ORDER BY rt.capacity ASC")
		List<RestaurantTable> findAvailableTablesByMinCapacity(@Param("minCapacity") Integer minCapacity);
		
		// 查詢有進行中訂單的桌子
		@Query("SELECT DISTINCT rt FROM RestaurantTable rt " +
			   "LEFT JOIN rt.orderGroups og " +
			   "WHERE og.status = true")
		List<RestaurantTable> findTablesWithActiveOrders();
		
		// 查詢沒有進行中訂單的桌子
		@Query("SELECT rt FROM RestaurantTable rt " +
			   "WHERE rt.id NOT IN (" +
			   "    SELECT DISTINCT rt2.id FROM RestaurantTable rt2 " +
			   "    LEFT JOIN rt2.orderGroups og " +
			   "    WHERE og.status = true" +
			   ")")
		List<RestaurantTable> findTablesWithoutActiveOrders();
		
		// 根據狀態查詢桌子
		@Query("SELECT rt FROM RestaurantTable rt WHERE rt.isAvailable = :status ORDER BY rt.tableId ASC")
		List<RestaurantTable> findByStatus(@Param("status") String status);
		
		// 查詢指定容量範圍的桌子
		@Query("SELECT rt FROM RestaurantTable rt WHERE rt.capacity BETWEEN :minCapacity AND :maxCapacity ORDER BY rt.capacity ASC")
		List<RestaurantTable> findByCapacityRange(@Param("minCapacity") Integer minCapacity, @Param("maxCapacity") Integer maxCapacity);
		
		// 統計各狀態桌子數量
		@Query("SELECT rt.isAvailable, COUNT(rt) FROM RestaurantTable rt GROUP BY rt.isAvailable")
		List<Object[]> getTableStatusCounts();
		
		List<RestaurantTable> findByIsAvailable(String isAvailable);

}
