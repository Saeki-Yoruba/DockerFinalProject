package com.supernovapos.finalproject.cart.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.order.model.Orders;

public interface OrderRepository extends JpaRepository<Orders, Long> {

	// 未提交 (購物車)
	@Query("SELECT o FROM Orders o WHERE o.orderGroup.id = :groupId AND o.status = false")
	List<Orders> findUnsubmittedByGroupId(@Param("groupId") UUID groupId);

	// 已提交
	@Query("SELECT o FROM Orders o WHERE o.orderGroup.id = :groupId AND o.status = true")
	List<Orders> findSubmittedByGroupId(@Param("groupId") UUID groupId);
}
