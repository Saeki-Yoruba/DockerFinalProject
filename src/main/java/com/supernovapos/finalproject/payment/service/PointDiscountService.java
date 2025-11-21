package com.supernovapos.finalproject.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.repository.UserRepository;

@Service
public class PointDiscountService {

	 private static final Logger log = LoggerFactory.getLogger(PointDiscountService.class);
	    
	    @Autowired
	    private PointService pointService;
	    
	    @Autowired
	    private UserRepository userRepository;
	    
	    // 點數兌換率：1點 = N元
	    @Value("${point.exchange.rate:1}")
	    private Integer pointExchangeRate;
	    
	    // 單筆訂單最大點數使用比例 (例如：0.8 = 最多使用80%)
	    @Value("${point.max.usage.ratio:1.0}")
	    private Double maxUsageRatio;
	    
	    public PointDiscountCalculation calculateDiscount(Long userId, Integer requestedPoints, Integer orderAmount) {
	        
	        log.info("🧮 計算點數折抵 - 用戶: {}, 要使用: {}點, 訂單金額: NT${}", 
	                 userId, requestedPoints, orderAmount);
	        
	        // 1. 檢查用戶是否存在
	        if (userId == null) {
	            return PointDiscountCalculation.createGuestResult();
	        }
	        
	        User user = userRepository.findById(userId)
	                .orElseThrow(() -> new RuntimeException("找不到用戶"));
	        
	        // 2. 取得用戶當前點數餘額
	        Integer userBalance = pointService.getCurrentUserBalance(userId);
	        
	        // 3. 計算最大可用點數
	        Integer maxUsablePoints = calculateMaxUsablePoints(orderAmount, userBalance);
	        
	        // 4. 決定實際使用點數
	        Integer actualPointsToUse = Math.min(requestedPoints, maxUsablePoints);
	        
	        // 5. 計算折抵金額
	        Integer discountAmount = actualPointsToUse * pointExchangeRate;
	        
	        // 6. 計算最終付款金額
	        Integer finalPayAmount = orderAmount - discountAmount;
	        
	        // 7. 計算使用點數後仍可獲得的新點數
	        Integer newEarnPoints = pointService.calculateEarnedPoints(finalPayAmount);
	        
	        log.info("✅ 點數折抵計算完成 - 使用: {}點, 折抵: NT${}, 實付: NT${}, 新獲得: {}點", 
	                 actualPointsToUse, discountAmount, finalPayAmount, newEarnPoints);
	        
	        return PointDiscountCalculation.builder()
	                .userId(userId)
	                .userCurrentBalance(userBalance)
	                .requestedPoints(requestedPoints)
	                .maxUsablePoints(maxUsablePoints)
	                .actualPointsToUse(actualPointsToUse)
	                .discountAmount(discountAmount)
	                .originalAmount(orderAmount)
	                .finalPayAmount(finalPayAmount)
	                .newEarnPoints(newEarnPoints)
	                .valid(true)
	                .build();
	    }
	    
	    
	      // 計算最大可用點數
	     
	    private Integer calculateMaxUsablePoints(Integer orderAmount, Integer userBalance) {
	        
	        // 1. 根據訂單金額計算最大可折抵金額
	        Integer maxDiscountAmount = (int) (orderAmount * maxUsageRatio);
	        
	        // 2. 轉換為點數
	        Integer maxDiscountPoints = maxDiscountAmount / pointExchangeRate;
	        
	        // 3. 不能超過用戶餘額
	        return Math.min(maxDiscountPoints, userBalance);
	    }
	    
	    /**
	     * ✅ 驗證點數使用請求
	     */
	    public ValidationResult validatePointUsage(Long userId, Integer pointsToUse, Integer orderAmount) {
	        
	        if (userId == null) {
	            return ValidationResult.error("Guest用戶無法使用點數");
	        }
	        
	        if (pointsToUse == null || pointsToUse <= 0) {
	            return ValidationResult.success("不使用點數");
	        }
	        
	        // 檢查用戶餘額
	        Integer userBalance = pointService.getCurrentUserBalance(userId);
	        if (userBalance < pointsToUse) {
	            return ValidationResult.error(
	                String.format("點數餘額不足，當前: %d點，需要: %d點", userBalance, pointsToUse)
	            );
	        }
	        
	        // 檢查使用限制
	        Integer maxUsable = calculateMaxUsablePoints(orderAmount, userBalance);
	        if (pointsToUse > maxUsable) {
	            return ValidationResult.error(
	                String.format("超過單筆使用限制，最多可用: %d點", maxUsable)
	            );
	        }
	        
	        return ValidationResult.success("點數使用驗證通過");
	    }
	    
	    
	 // 📊 點數折抵計算結果
	    @lombok.Data
	    @lombok.Builder
	    @lombok.AllArgsConstructor
	    @lombok.NoArgsConstructor
	    public static class PointDiscountCalculation {
	        private Long userId;
	        private Integer userCurrentBalance;        // 用戶當前餘額
	        private Integer requestedPoints;          // 請求使用點數
	        private Integer maxUsablePoints;          // 最大可用點數
	        private Integer actualPointsToUse;       // 實際使用點數
	        private Integer discountAmount;           // 折抵金額
	        private Integer originalAmount;           // 原始訂單金額
	        private Integer finalPayAmount;           // 最終付款金額
	        private Integer newEarnPoints;            // 本次可獲得新點數
	        private Boolean valid;                    // 計算是否有效
	        private String message;                   // 說明訊息
	        
	        // 建立Guest用戶結果
	        public static PointDiscountCalculation createGuestResult() {
	            return PointDiscountCalculation.builder()
	                    .userId(null)
	                    .userCurrentBalance(0)
	                    .requestedPoints(0)
	                    .maxUsablePoints(0)
	                    .actualPointsToUse(0)
	                    .discountAmount(0)
	                    .valid(false)
	                    .message("Guest用戶無法使用點數")
	                    .build();
	        }
	        
	        // 格式化顯示
	        public String getFormattedDiscount() {
	            return discountAmount > 0 ? String.format("- NT$ %d", discountAmount) : "";
	        }
	        
	        public String getFormattedFinalAmount() {
	            return String.format("NT$ %d", finalPayAmount);
	        }
	        
	        public boolean isUsingPoints() {
	            return actualPointsToUse != null && actualPointsToUse > 0;
	        }
	    }
	    
	    // 內部：驗證結果
	    @lombok.Data
	    @lombok.AllArgsConstructor
	    public static class ValidationResult {
	        private Boolean success;
	        private String message;
	        
	        public static ValidationResult success(String message) {
	            return new ValidationResult(true, message);
	        }
	        
	        public static ValidationResult error(String message) {
	            return new ValidationResult(false, message);
	        }
	    }
}
