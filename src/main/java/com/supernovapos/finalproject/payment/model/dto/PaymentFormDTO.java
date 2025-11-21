package com.supernovapos.finalproject.payment.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFormDTO {
	
	// 綠界表單相關
	private String action; //綠界URL
	
	private String merchantTradeNo; // 商家交易編號(給綠界
	
	private Integer totalAmount;              // 總付款金額
	
	private String itemName;                  // 綠界商品名稱（簡化版）
	
	private Map<String, String> formData;     // 綠界表單參數
	
	// 商品明細相關
	
	private List<PaymentItemDTO> itemDetails; // 所有商品明細
	
	private List<OrdererInfoDTO> ordererDetails; // 按點餐人分組的明細
	
	// 金額資訊
	
	private Integer originalAmount;           // 原始總金額
    private Integer actualPayAmount;          // 實際付款金額
	
    // 其他資訊
    
    private String tableInfo;                 // 桌號資訊（如果有）
    
    private LocalDateTime orderTime;          // 點餐時間
	
    // 點數相關資訊
    private Integer expectedEarnPoints;		  // 預計獲得點數
    
    private String memberInfo;				  // 會員資訊( Guest用戶/ 會員用戶)
    
    private Integer currentUserPoints;		  // 用戶當前點數餘額 (如果是會員)
    
    private String pointsEarnRule;			  // 點數累積說明
    
    // 點數使用相關
    
    private Integer pointsUsed;				  // 本次使用的點數
    
    private Integer pointsDiscount;			  // 點數折抵金額
    
    // 格式化顯示方法
    public String getFormattedOriginalAmount() {
        return "NT$ " + originalAmount;
    }
    
    public String getFormattedActualPayAmount() {
        return "NT$ " + actualPayAmount;
    }
    
//   // 🆕 格式化點數顯示
    public String getFormattedExpectedPoints() {
        if (expectedEarnPoints == null || expectedEarnPoints <= 0) {
            return "無點數累積";
        }
        return "+" + expectedEarnPoints + " 點";
    }
    
    public String getFormattedCurrentPoints() {
        if (currentUserPoints == null) {
            return "N/A";
        }
        return currentUserPoints + " 點";
    }
    
    // 🆕 格式化點數使用顯示
    public String getFormattedPointsUsed() {
        if (pointsUsed == null || pointsUsed <= 0) {
            return "";
        }
        return pointsUsed + " 點";
    }
    
    public String getFormattedPointsDiscount() {
        if (pointsDiscount == null || pointsDiscount <= 0) {
            return "";
        }
        return "- NT$ " + pointsDiscount;
    }
    
    // 🆕 檢查是否為會員
    public boolean isMemberPayment() {
        return expectedEarnPoints != null && expectedEarnPoints > 0;
    }
    
    // 🆕 檢查是否使用了點數
    public boolean isUsingPoints() {
        return pointsUsed != null && pointsUsed > 0;
    }
    
    // 🆕 取得點數累積說明
    public String getPointsDescription() {
        if (!isMemberPayment()) {
            return "登入會員即可累積點數！";
        }
        
        if (isUsingPoints()) {
            return String.format("使用 %d 點折抵 NT$%d，實付金額將獲得 %d 點", 
                                pointsUsed, pointsDiscount, expectedEarnPoints);
        }
        
        return String.format("本次消費將獲得 %d 點，點數可於下次消費使用", expectedEarnPoints);
    }
    
    // 🆕 取得完整的金額明細說明
    public String getPaymentSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("訂單金額: NT$ ").append(originalAmount);
        
        if (isUsingPoints()) {
            summary.append("\n點數折抵: ").append(getFormattedPointsDiscount());
            summary.append(" (").append(pointsUsed).append("點)");
        }
        
        summary.append("\n實付金額: ").append(getFormattedActualPayAmount());
        
        if (isMemberPayment()) {
            summary.append("\n預計獲得: ").append(getFormattedExpectedPoints());
        }
        
        return summary.toString();
    }
}
