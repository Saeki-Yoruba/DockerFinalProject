	package com.supernovapos.finalproject.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.supernovapos.finalproject.payment.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	
	
	// 基本查詢
    Optional<Payment> findByMerchantTradeNo(String merchantTradeNo);
    Optional<Payment> findByOrderGroupId(UUID orderGroupId);
    List<Payment> findByPayerUserIdOrderByCreatedAtDesc(Long payerUserId);
    
    // 狀態檢查
    boolean existsByOrderGroupId(UUID orderGroupId);
    List<Payment> findByTradeStatus(String tradeStatus);
    
 // 時間範圍查詢
    List<Payment> findByTradeStatusAndCreatedAtBetween(
        String tradeStatus, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // 測試用
    List<Payment> findBySimulatePaid(Boolean simulatePaid);
    
    // 統計查詢
    @Query("SELECT SUM(p.totalAmount) FROM Payment p WHERE p.tradeStatus = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
    Long getTotalSuccessAmountBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tradeStatus = 'SUCCESS' AND p.payerUserId = :userId")
    Long getSuccessPaymentCountByUser(@Param("userId") Long userId);

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//    @Query(value = """
//            SELECT new com.supernovapos.finalproject.payment.PaymentsDTO(
//                oi.quantity,
//                p.name,
//                oi.unitPrice,
//                og.totalAmount,
//                og.tableId,
//                COALESCE(po.pointsAmount, 0),
//                pay.choosePayment,
//                o.customerName
//            )
//            FROM OrderItems oi
//            LEFT JOIN Products p ON oi.productId = p.id
//            LEFT JOIN Order o ON o.id = oi.orderId
//            LEFT JOIN OrderGroup og ON og.id = o.groupId
//            LEFT JOIN Payment pay ON pay.orderGroupId = og.id
//            LEFT JOIN Point po ON po.paymentId = pay.id
//            WHERE og.id = :groupId
//            """)
//        List<PaymentsDTO> getOrderDetailsByGroupId(@Param("groupId") Long groupId);
//    
}

