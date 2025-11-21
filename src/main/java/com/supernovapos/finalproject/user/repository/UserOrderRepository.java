package com.supernovapos.finalproject.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.supernovapos.finalproject.order.model.Orders;

public interface UserOrderRepository extends JpaRepository<Orders, Long>{
	
	Page<Orders> findByUserId(Long userId, Pageable pageable);
}
