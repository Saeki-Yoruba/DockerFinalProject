package com.supernovapos.finalproject.payment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.payment.service.EmailService;
import com.supernovapos.finalproject.payment.model.Payment;
import com.supernovapos.finalproject.payment.repository.PaymentRepository;
import com.supernovapos.finalproject.payment.service.OrderDataService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/email")
public class EmailTestController {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private OrderDataService orderDataService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * 測試基本郵件發送
     */
    @PostMapping("/test")
    @PreAuthorize("isAuthenticated()")
	@Operation(summary = "測試基本郵件發送功能", description = "發送簡單的測試郵件到指定信箱，用於驗證郵件服務配置是否正確。此API僅供測試用途，需要登入後才能使用。")
    public ResponseEntity<Map<String, String>> sendTestEmail(
            @RequestParam String email) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            emailService.sendTestEmail(email);
            response.put("success", "true");
            response.put("message", "測試郵件發送成功");
            
        } catch (Exception e) {
            response.put("success", "false");
            response.put("message", "測試郵件發送失敗: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 測試訂單確認郵件
     */
    @PostMapping("/test-order")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "測試訂單確認郵件發送", description = "根據商家交易編號查找對應的付款記錄，收集訂單詳細資料後發送訂單確認郵件。\" +\r\n"
    		+"可選擇性覆蓋收件人郵箱進行測試。需要登入後才能使用。")
    public ResponseEntity<Map<String, String>> sendTestOrderEmail(
            @RequestBody TestOrderEmailRequest request) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // 根據 merchantTradeNo 查找付款記錄
            Payment payment = paymentRepository.findByMerchantTradeNo(request.getMerchantTradeNo())
                    .orElseThrow(() -> new RuntimeException("找不到付款記錄"));
            
            // 收集訂單資料
            var orderEmailData = orderDataService.collectOrderDataForEmail(payment);
            
            // 如果指定了測試郵箱，覆蓋原有郵箱
            if (request.getTestEmail() != null && !request.getTestEmail().isEmpty()) {
                orderEmailData.setMemberEmail(request.getTestEmail());
            }
            
            // 發送郵件
            emailService.sendOrderConfirmationEmail(orderEmailData);
            
            response.put("success", "true");
            response.put("message", "訂單確認郵件發送成功");
            response.put("recipient", orderEmailData.getMemberEmail());
            
        } catch (Exception e) {
            response.put("success", "false");
            response.put("message", "訂單確認郵件發送失敗: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @lombok.Data
    public static class TestOrderEmailRequest {
        private String merchantTradeNo;
        private String testEmail; // 可選，用於測試時覆蓋原有郵箱
    }
}