package com.supernovapos.finalproject.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.payment.model.Payment;
import com.supernovapos.finalproject.payment.model.Point;
import com.supernovapos.finalproject.payment.model.Point.PointType;
import com.supernovapos.finalproject.payment.repository.PointRepository;
import com.supernovapos.finalproject.store.model.Store;
import com.supernovapos.finalproject.store.repository.StoreRepository;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
@Transactional
public class PointService {

private static final Logger log = LoggerFactory.getLogger(PointService.class);
    
    @Autowired
    private PointRepository pointRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    private Integer pointEarnRate;
    
    // 可設定的點數規則
    @PostConstruct
    private void init() {
        Store store = storeRepository.findTopByOrderByIdAsc().get();
        this.pointEarnRate = store.getPointsPerCurrency().intValue();
    }
    @Value("${point.expiry.months:12}")
    private Integer pointExpiryMonths; // 點數有效期，預設12個月
    
    // 核心方法：付款成功後累積點數
    public Point earnPointsFromPayment(Payment payment) {
        log.info("🎯 開始為付款累積點數 - PaymentId: {}, PayerUserId: {}, Amount: {}", 
                 payment.getId(), payment.getPayerUserId(), payment.getTotalAmount());
        
        // 1. 檢查是否已經累積過點數（防重複）
        if (pointRepository.existsByPaymentId(payment.getId())) {
            log.warn("⚠️ 付款ID {} 已累積過點數，跳過", payment.getId());
            return null;
        }
        
        // 2. 檢查付款人是否為註冊會員
        if (payment.getPayerUserId() == null) {
            log.info(" 訪客付款，不累積點數");
            return null;
        }
        
        User user = userRepository.findById(payment.getPayerUserId())
                .orElseThrow(() -> new RuntimeException("找不到用戶: " + payment.getPayerUserId()));
        
        // 3. 計算獲得點數（每10元1點）
        Integer earnedPoints = calculateEarnedPoints(payment.getTotalAmount());
        
        if (earnedPoints <= 0) {
            log.info(" 付款金額 {} 不足以獲得點數", payment.getTotalAmount());
            return null;
        }
        
        // 4. 取得用戶當前餘額
        Integer currentBalance = getCurrentUserBalance(user.getId());
        Integer newBalance = currentBalance + earnedPoints;
        
        // 5. 建立點數記錄
        Point pointRecord = Point.builder()
                .user(user)
                .type(PointType.ORDER_EARN)
                .pointsAmount(earnedPoints)
                .balanceAfter(newBalance)
                .orderGroupId(payment.getOrderGroupId())
                .payment(payment)
                .expiredAt(LocalDateTime.now().plusMonths(pointExpiryMonths))
                .isExpired(false)
                .description(String.format("消費累積點數 - 訂單金額: NT$%d", payment.getTotalAmount()))
                .createdAt(LocalDateTime.now())
                .build();
        
        Point savedPoint = pointRepository.save(pointRecord);
        
        // 6. 更新用戶總點數
        user.setPoint(newBalance);
        userRepository.save(user);
        
        log.info(" 點數累積成功 - 用戶: {}, 獲得: {}點, 餘額: {}點", 
                 user.getId(), earnedPoints, newBalance);
        
        return savedPoint;
    }
    
    /**
     *  計算付款應獲得的點數
     */
    public Integer calculateEarnedPoints(Integer paymentAmount) {
        if (paymentAmount == null || paymentAmount <= 0) {
            return 0;
        }
        return paymentAmount / pointEarnRate;
    }
    
    /**
     *  取得用戶當前點數餘額
     */
    public Integer getCurrentUserBalance(Long userId) {
        return pointRepository.getCurrentBalance(userId).orElse(0);
    }
    
    /**
     * 📊 取得用戶點數歷史記錄
     */
    public List<Point> getUserPointHistory(Long userId) {
        return pointRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 🎁 管理員手動給予點數
     */
    public Point grantPointsByAdmin(Long userId, Integer points, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到用戶"));
        
        Integer currentBalance = getCurrentUserBalance(userId);
        Integer newBalance = currentBalance + points;
        
        Point pointRecord = Point.builder()
                .user(user)
                .type(PointType.ADMIN_GRANT)
                .pointsAmount(points)
                .balanceAfter(newBalance)
                .description("管理員給予: " + reason)
                .isExpired(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        Point savedPoint = pointRepository.save(pointRecord);
        
        // 更新用戶點數
        user.setPoint(newBalance);
        userRepository.save(user);
        
        log.info(" 管理員給予點數 - 用戶: {}, 點數: {}, 原因: {}", userId, points, reason);
        return savedPoint;
    }
    
    /**
     *  扣除用戶點數（用於消費抵扣）
     */
    public Point deductPoints(Long userId, Integer points, UUID orderGroupId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到用戶"));
        
        Integer currentBalance = getCurrentUserBalance(userId);
        
        if (currentBalance < points) {
            throw new RuntimeException("點數餘額不足，當前: " + currentBalance + "，需要: " + points);
        }
        
        Integer newBalance = currentBalance - points;
        
        Point pointRecord = Point.builder()
                .user(user)
                .type(PointType.ORDER_USE)
                .pointsAmount(-points) // 負數表示扣除
                .balanceAfter(newBalance)
                .orderGroupId(orderGroupId)
                .description(reason)
                .createdAt(LocalDateTime.now())
                .build();
        
        Point savedPoint = pointRepository.save(pointRecord);
        
        // 更新用戶點數
        user.setPoint(newBalance);
        userRepository.save(user);
        
        log.info(" 點數扣除成功 - 用戶: {}, 扣除: {}點, 餘額: {}點", userId, points, newBalance);
        return savedPoint;
    }
    
    /**
     *  付款時使用點數折抵 (與 Payment 綁定)
     */
    public Point usePointsForPayment(Payment payment, Integer pointsToUse) {
        
        if (pointsToUse == null || pointsToUse <= 0) {
            log.info("💡 本次付款不使用點數");
            return null;
        }
        
        if (payment.getPayerUserId() == null) {
            throw new RuntimeException("Guest用戶無法使用點數");
        }
        
        log.info("🛒 處理付款點數使用 - PaymentId: {}, 使用點數: {}", 
                 payment.getId(), pointsToUse);
        
        // 1. 驗證用戶餘額
        Integer currentBalance = getCurrentUserBalance(payment.getPayerUserId());
        if (currentBalance < pointsToUse) {
            throw new RuntimeException(
                String.format("點數餘額不足，當前: %d點，需要: %d點", currentBalance, pointsToUse)
            );
        }
        
        // 2. 建立點數使用記錄
        User user = userRepository.findById(payment.getPayerUserId())
                .orElseThrow(() -> new RuntimeException("找不到用戶"));
        
        Integer newBalance = currentBalance - pointsToUse;
        Integer discountAmount = pointsToUse * 1; // 1點=1元，可從設定檔讀取
        
        Point pointRecord = Point.builder()
                .user(user)
                .type(PointType.ORDER_USE)
                .pointsAmount(-pointsToUse)
                .balanceAfter(newBalance)
                .orderGroupId(payment.getOrderGroupId())
                .payment(payment)
                .isExpired(false)
                .description(String.format("消費使用點數 - 折抵金額: NT$%d", discountAmount))
                .createdAt(LocalDateTime.now())
                .build();
        
        Point savedPoint = pointRepository.save(pointRecord);
        
        // 3. 更新用戶總點數
        user.setPoint(newBalance);
        userRepository.save(user);
        
        log.info("✅ 付款點數使用成功 - 用戶: {}, 使用: {}點, 餘額: {}點, 折抵: NT${}",
                 user.getId(), pointsToUse, newBalance, discountAmount);
        
        return savedPoint;
    }
    
    /**
     *  處理點數過期（定時任務用）
     */
    public void processExpiredPoints() {
        LocalDateTime now = LocalDateTime.now();
        List<Point> expiringPoints = pointRepository.findAllExpiringPoints(now);
        
        for (Point expiredPoint : expiringPoints) {
            // 標記為過期
            expiredPoint.setIsExpired(true);
            pointRepository.save(expiredPoint);
            
            // 建立過期扣除記錄
            Integer currentBalance = getCurrentUserBalance(expiredPoint.getUser().getId());
            Integer newBalance = currentBalance - expiredPoint.getPointsAmount();
            
            Point expireRecord = Point.builder()
                    .user(expiredPoint.getUser())
                    .type(PointType.EXPIRED)
                    .pointsAmount(-expiredPoint.getPointsAmount())
                    .balanceAfter(newBalance)
                    .description("點數過期扣除")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            pointRepository.save(expireRecord);
            
            // 更新用戶總點數
            User user = expiredPoint.getUser();
            user.setPoint(newBalance);
            userRepository.save(user);
            
            log.info("⏰ 點數過期處理 - 用戶: {}, 過期點數: {}", 
                     user.getId(), expiredPoint.getPointsAmount());
        }
    }
    
    /**
     * 取得用戶點數統計
     */
    public PointStatistics getUserPointStatistics(Long userId) {
        Integer currentBalance = getCurrentUserBalance(userId);
        Integer totalEarned = pointRepository.getTotalEarnedPoints(userId);
        Integer totalUsed = pointRepository.getTotalUsedPoints(userId);
        
        return PointStatistics.builder()
                .currentBalance(currentBalance)
                .totalEarned(totalEarned)
                .totalUsed(totalUsed)
                .build();
    }
    
    // 內部類：點數統計資料
    @lombok.Data
    @lombok.Builder
    public static class PointStatistics {
        private Integer currentBalance;
        private Integer totalEarned;
        private Integer totalUsed;
    }
}
