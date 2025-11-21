package com.supernovapos.finalproject.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.supernovapos.finalproject.payment.model.Point;
import com.supernovapos.finalproject.payment.model.Point.PointType;

@Repository
public interface PointRepository extends JpaRepository<Point, Integer> {
    
    // === 基本查詢 ===
    
    /**
     * 查詢用戶的點數歷史記錄（按時間降序）
     */
    List<Point> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 分頁查詢用戶點數歷史
     */
    Page<Point> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根據類型查詢用戶點數記錄
     */
    List<Point> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, PointType type);
    
    /**
     * 查詢特定訂單群組的點數記錄
     */
    List<Point> findByOrderGroupId(UUID orderGroupId);
    
    /**
     * 查詢特定付款的點數記錄
     */
    Optional<Point> findByPaymentId(Long paymentId);
    
    // === 統計查詢 ===
    
    /**
     * 計算用戶當前點數餘額（最新記錄的 balance_after）
     */
    @Query("SELECT p.balanceAfter FROM Point p WHERE p.user.id = :userId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Integer> getCurrentBalance(@Param("userId") Long userId);
    
    /**
     * 計算用戶總獲得點數
     */
    @Query("SELECT COALESCE(SUM(p.pointsAmount), 0) FROM Point p WHERE p.user.id = :userId AND p.pointsAmount > 0")
    Integer getTotalEarnedPoints(@Param("userId") Long userId);
    
    /**
     * 計算用戶總使用點數
     */
    @Query("SELECT COALESCE(SUM(ABS(p.pointsAmount)), 0) FROM Point p WHERE p.user.id = :userId AND p.pointsAmount < 0")
    Integer getTotalUsedPoints(@Param("userId") Long userId);
    
    /**
     * 計算時間範圍內的點數異動
     */
    @Query("SELECT COALESCE(SUM(p.pointsAmount), 0) FROM Point p WHERE p.user.id = :userId AND p.createdAt BETWEEN :startDate AND :endDate")
    Integer getPointsInDateRange(@Param("userId") Long userId, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    // === 過期相關查詢 ===
    
    /**
     * 查詢即將過期的點數（未過期且過期時間在指定日期前）
     */
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId AND p.isExpired = false AND p.expiredAt IS NOT NULL AND p.expiredAt <= :expireDate")
    List<Point> findExpiringPoints(@Param("userId") Long userId, @Param("expireDate") LocalDateTime expireDate);
    
    /**
     * 查詢所有即將過期的點數（系統級別）
     */
    @Query("SELECT p FROM Point p WHERE p.isExpired = false AND p.expiredAt IS NOT NULL AND p.expiredAt <= :expireDate")
    List<Point> findAllExpiringPoints(@Param("expireDate") LocalDateTime expireDate);
    
    // === 管理後台統計 ===
    
    /**
     * 今日新增點數總量
     */
    @Query("SELECT COALESCE(SUM(p.pointsAmount), 0) FROM Point p WHERE p.pointsAmount > 0 AND p.createdAt >= :startOfDay")
    Integer getTodayEarnedPoints(@Param("startOfDay") LocalDateTime startOfDay);
    
    /**
     * 今日使用點數總量
     */
    @Query("SELECT COALESCE(SUM(ABS(p.pointsAmount)), 0) FROM Point p WHERE p.pointsAmount < 0 AND p.createdAt >= :startOfDay")
    Integer getTodayUsedPoints(@Param("startOfDay") LocalDateTime startOfDay);
    
    /**
     * 檢查是否已經為某筆付款建立點數記錄（防重複）
     */
    boolean existsByPaymentId(Long paymentId);
    
    /**
     * 根據訂單群組和類型查詢
     */
    List<Point> findByOrderGroupIdAndType(UUID orderGroupId, PointType type);
}