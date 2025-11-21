package com.supernovapos.finalproject.payment.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.payment.model.Point;
import com.supernovapos.finalproject.payment.model.dto.PointHistoryDTO;
import com.supernovapos.finalproject.payment.service.PointDiscountService;
import com.supernovapos.finalproject.payment.service.PointService;
import com.supernovapos.finalproject.payment.service.PointService.PointStatistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/points")
@Tag(name = "點數管理", description = "會員點數累積、查詢、管理功能")
public class PointController {

	 @Autowired
	 private PointService pointService;
	 
	 @Autowired
	 private PointDiscountService pointDiscountService;
	 
	 @Operation(summary = "查詢用戶點數餘額", description = "取得指定用戶的當前點數餘額")
	    @GetMapping("/balance/{userId}")
	    @PreAuthorize("isAuthenticated()") // 可根據需求調整權限
	    public ResponseEntity<Map<String, Object>> getUserPointBalance(
	            @Parameter(description = "用戶ID") @PathVariable Long userId) {
	        
	        Integer balance = pointService.getCurrentUserBalance(userId);
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("userId", userId);
	        response.put("pointBalance", balance);
	        response.put("formattedBalance", balance + " 點");
	        
	        return ResponseEntity.ok(response);
	    }

	    /**
	     * 📊 查詢用戶點數統計
	     */
	    @Operation(summary = "查詢用戶點數統計", description = "取得用戶的點數統計資訊（總獲得、總使用、當前餘額）")
	    @GetMapping("/statistics/{userId}")
	    @PreAuthorize("isAuthenticated()")
	    public ResponseEntity<PointStatistics> getUserPointStatistics(
	            @Parameter(description = "用戶ID") @PathVariable Long userId) {
	        
	        PointStatistics statistics = pointService.getUserPointStatistics(userId);
	        return ResponseEntity.ok(statistics);
	    }

	    /**
	     * 📝 查詢用戶點數歷史記錄
	     */
	    @Operation(summary = "查詢點數歷史記錄", description = "取得用戶的所有點數異動記錄")
	    @GetMapping("/history/{userId}")
	    @PreAuthorize("isAuthenticated()")
	    public ResponseEntity<List<PointHistoryDTO>> getUserPointHistory(
	            @Parameter(description = "用戶ID") @PathVariable Long userId) {
	        
	        List<Point> points = pointService.getUserPointHistory(userId);
	      List <PointHistoryDTO> dtoList = points.stream()
	    		  .map(PointHistoryDTO::fromEntity)
	    		  .collect(Collectors.toList());
	      
	        return ResponseEntity.ok(dtoList);
	    }

	    /**
	     * 🧮 計算付款可獲得點數（預覽用）
	     */
	    @Operation(summary = "計算可獲得點數", description = "根據付款金額計算可獲得的點數，供前端顯示用")
	    @GetMapping("/calculate")
	    @PreAuthorize("isAuthenticated()")
	    public ResponseEntity<Map<String, Object>> calculateEarnablePoints(
	            @Parameter(description = "付款金額") @RequestParam Integer amount) {
	        
	        Integer earnablePoints = pointService.calculateEarnedPoints(amount);
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("paymentAmount", amount);
	        response.put("earnablePoints", earnablePoints);
	        response.put("description", String.format("消費 NT$%d 可獲得 %d 點", amount, earnablePoints));
	        
	        return ResponseEntity.ok(response);
	    }

	    /**
	     * 🎯 計算點數折抵預覽（結帳前使用）
	     */
	    @Operation(summary = "計算點數折抵預覽", description = "計算使用指定點數的折抵效果，供結帳頁面預覽")
	    @PostMapping("/preview-discount")
	    @PreAuthorize("isAuthenticated()")
	    public ResponseEntity<?> previewPointDiscount(@RequestBody PreviewDiscountRequest request) {
	        
	        try {
	            request.validate();
	            
	            // 使用點數折抵服務計算
	            var calculation = pointDiscountService.calculateDiscount(
	                request.getUserId(), 
	                request.getPointsToUse(), 
	                request.getOrderAmount()
	            );
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("calculation", calculation);
	            response.put("summary", buildDiscountSummary(calculation));
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", false);
	            response.put("message", "計算失敗: " + e.getMessage());
	            
	            return ResponseEntity.badRequest().body(response);
	        }
	    }
	    
	    /**
	     * 📊 取得用戶可用點數資訊（結帳頁面用）
	     */
	    @Operation(summary = "取得用戶可用點數資訊", description = "取得用戶當前點數餘額和使用規則，供結帳頁面顯示")
	    @GetMapping("/checkout-info/{userId}")
	    @PreAuthorize("isAuthenticated()")
	    public ResponseEntity<Map<String, Object>> getCheckoutPointInfo(
	            @Parameter(description = "用戶ID") @PathVariable Long userId,
	            @Parameter(description = "訂單金額") @RequestParam Integer orderAmount) {
	        
	        try {
	            Integer userBalance = pointService.getCurrentUserBalance(userId);
	            var calculation = pointDiscountService.calculateDiscount(userId, userBalance, orderAmount);
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("userId", userId);
	            response.put("currentBalance", userBalance);
	            response.put("maxUsablePoints", calculation.getMaxUsablePoints());
	            response.put("orderAmount", orderAmount);
	            response.put("exchangeRate", "1點 = 1元");
	            response.put("earnRule", "每10元 = 1點");
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("error", "查詢失敗: " + e.getMessage());
	            
	            return ResponseEntity.badRequest().body(response);
	        }
	    }

	    /**
	     * 🎁 管理員給予點數
	     */
	    @Operation(summary = "管理員給予點數", description = "管理員手動給予用戶點數", security = @SecurityRequirement(name = "bearerAuth"))
	    @PostMapping("/admin/grant")
	    @PreAuthorize("hasRole('ADMIN')") // 需要管理員權限
	    public ResponseEntity<Map<String, Object>> grantPointsByAdmin(
	            @RequestBody GrantPointsRequest request) {
	        
	        try {
	            Point pointRecord = pointService.grantPointsByAdmin(
	                request.getUserId(), 
	                request.getPoints(), 
	                request.getReason()
	            );
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("message", "點數給予成功");
	            response.put("pointRecord", pointRecord);
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", false);
	            response.put("message", "點數給予失敗: " + e.getMessage());
	            
	            return ResponseEntity.badRequest().body(response);
	        }
	    }

	    /**
	     * ⚡ 處理點數過期（管理員觸發）
	     */
	    @Operation(summary = "處理過期點數", description = "手動觸發點數過期處理程序", security = @SecurityRequirement(name = "bearerAuth"))
	    @PostMapping("/admin/process-expired")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<Map<String, String>> processExpiredPoints() {
	        
	        try {
	            pointService.processExpiredPoints();
	            
	            Map<String, String> response = new HashMap<>();
	            response.put("success", "true");
	            response.put("message", "過期點數處理完成");
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            Map<String, String> response = new HashMap<>();
	            response.put("success", "false");
	            response.put("message", "處理失敗: " + e.getMessage());
	            
	            return ResponseEntity.badRequest().body(response);
	        }
	    }

	    // 🗂️ 內部類：請求DTO
	    
	    @lombok.Data
	    public static class GrantPointsRequest {
	        private Long userId;
	        private Integer points;
	        private String reason;
	        
	        // 驗證方法
	        public void validate() {
	            if (userId == null) {
	                throw new IllegalArgumentException("用戶ID不能為空");
	            }
	            if (points == null || points <= 0) {
	                throw new IllegalArgumentException("點數必須大於0");
	            }
	            if (reason == null || reason.trim().isEmpty()) {
	                throw new IllegalArgumentException("給予原因不能為空");
	            }
	        }
	    }
	    
	    @lombok.Data
	    public static class PreviewDiscountRequest {
	        private Long userId;
	        private Integer pointsToUse;
	        private Integer orderAmount;
	        
	        public void validate() {
	            if (userId == null) {
	                throw new IllegalArgumentException("用戶ID不能為空");
	            }
	            if (pointsToUse == null || pointsToUse < 0) {
	                throw new IllegalArgumentException("使用點數不能為負數");
	            }
	            if (orderAmount == null || orderAmount <= 0) {
	                throw new IllegalArgumentException("訂單金額必須大於0");
	            }
	        }
	    }
	    
	    // 🛠️ 輔助方法
	    
	    private Map<String, Object> buildDiscountSummary(PointDiscountService.PointDiscountCalculation calc) {
	        Map<String, Object> summary = new HashMap<>();
	        summary.put("originalAmount", "NT$ " + calc.getOriginalAmount());
	        summary.put("pointsUsed", calc.getActualPointsToUse() + " 點");
	        summary.put("discountAmount", "- NT$ " + calc.getDiscountAmount());
	        summary.put("finalAmount", "NT$ " + calc.getFinalPayAmount());
	        summary.put("newEarnPoints", "+" + calc.getNewEarnPoints() + " 點");
	        summary.put("description", String.format(
	            "使用 %d 點折抵 NT$%d，實付 NT$%d 將獲得 %d 點",
	            calc.getActualPointsToUse(),
	            calc.getDiscountAmount(),
	            calc.getFinalPayAmount(),
	            calc.getNewEarnPoints()
	        ));
	        return summary;
	    }
}
