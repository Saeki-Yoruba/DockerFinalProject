package com.supernovapos.finalproject.payment.service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.order.model.OrderGroup;
import com.supernovapos.finalproject.order.model.OrderItems;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.order.repository.OrderGroupRepository;
import com.supernovapos.finalproject.order.repository.OrderItemsRepository;
import com.supernovapos.finalproject.order.repository.OrdersRepository;
import com.supernovapos.finalproject.order.service.OrderGroupService;
import com.supernovapos.finalproject.payment.EcpayConfig;
import com.supernovapos.finalproject.payment.model.Payment;
import com.supernovapos.finalproject.payment.model.dto.CreatePaymentRequestDTO;
import com.supernovapos.finalproject.payment.model.dto.OrderEmailDTO;
import com.supernovapos.finalproject.payment.model.dto.OrdererInfoDTO;
import com.supernovapos.finalproject.payment.model.dto.PaymentDetailDTO;
import com.supernovapos.finalproject.payment.model.dto.PaymentFormDTO;
import com.supernovapos.finalproject.payment.model.dto.PaymentItemDTO;
import com.supernovapos.finalproject.payment.model.dto.PaymentResultDTO;
import com.supernovapos.finalproject.payment.repository.PaymentRepository;
import com.supernovapos.finalproject.payment.service.PointDiscountService.PointDiscountCalculation;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.repository.UserRepository;

@Service
@Transactional
public class EcpayService {
    
    private static final Logger log = LoggerFactory.getLogger(EcpayService.class);
    
    @Autowired
    private PointService pointService;
    
    @Autowired
    private PointDiscountService pointDiscountService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderGroupRepository orderGroupRepository;
    
    @Autowired
    private EcpayConfig ecpayConfig;
    
    @Autowired
    private OrderGroupService orderGroupService;
    
    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private EmailService emailService;
    
    @Value("${restaurant.name:SupernovaPOS}")
    private String restaurantName;

    @Value("${restaurant.phone:0900-000-000}")
    private String restaurantPhone;
    
    @Value("${ecpay.merchant.id}")
    private String merchantId;
    
    @Value("${ecpay.hash.key}")
    private String hashKey;
    
    @Value("${ecpay.hash.iv}")
    private String hashIV;
    
    @Value("${payment.return.url}")
    private String returnUrl;
    
    @Value("${payment.callback.url}")
    private String callbackUrl;
    
    
     // 核心方法1：建立付款表單
     
    public PaymentFormDTO createPaymentForm(CreatePaymentRequestDTO request) {
        
    	// 判斷是訪客還是會員
        boolean isGuest = request.isGuest();
        
        log.info("🚀 建立付款表單 - 類型: {}, OrderGroupId: {}, 使用點數: {}",
                isGuest ? "訪客" : "會員", 
                request.getOrderGroupId(), 
                request.getSafePointsToUse());
        
        // 1. 驗證
        request.validate();
        
        OrderGroup orderGroup = orderGroupRepository.findById(request.getOrderGroupId())
                .orElseThrow(() -> new RuntimeException("找不到訂單"));
        
        if (orderGroup.getTotalAmount() <= 0) {
            throw new RuntimeException("訂單金額錯誤");
        }
        
        // 2. 檢查重複付款
        if (paymentRepository.existsByOrderGroupId(request.getOrderGroupId())) {
            throw new RuntimeException("該桌已有付款記錄");
        }
        
        // 3. 計算點數折抵（訪客跳過）
        PointDiscountCalculation discountCalc;
        
        if (isGuest) {
            // ✅ 訪客不計算折抵
            discountCalc = PointDiscountCalculation.builder()
                    .originalAmount(orderGroup.getTotalAmount())
                    .actualPointsToUse(0)
                    .discountAmount(0)
                    .finalPayAmount(orderGroup.getTotalAmount())
                    .newEarnPoints(0)  // 訪客不累積點數
                    .build();
            
            log.info("👤 訪客付款 - 無點數折抵，原價: NT${}", orderGroup.getTotalAmount());
            
        } else {
            // ✅ 會員計算折抵
            discountCalc = pointDiscountService.calculateDiscount(
                    request.getPayerUserId(), 
                    request.getSafePointsToUse(), 
                    orderGroup.getTotalAmount()
            );
            
            log.info("💰 點數折抵計算 - 原金額: NT${}, 使用點數: {}點, 折抵: NT${}, 實付: NT${}",
                    discountCalc.getOriginalAmount(), 
                    discountCalc.getActualPointsToUse(),
                    discountCalc.getDiscountAmount(),
                    discountCalc.getFinalPayAmount());
        }
        
        // 4. 建立Payment記錄
        Payment payment = new Payment();
        payment.setOrderGroupId(orderGroup.getId());
        payment.setPayerUserId(request.getPayerUserId());  // 可以是 null
        payment.setMerchantTradeNo(generateMerchantTradeNo());
        payment.setTradeDesc(isGuest ? "訪客消費" : "會員消費");
        payment.setPaymentType("aio");
        payment.setChoosePayment("Credit");
        payment.setTradeStatus("PENDING");
        
        // 設定點數相關欄位
        payment.setTotalAmount(discountCalc.getFinalPayAmount());
        payment.setPointsUsed(discountCalc.getActualPointsToUse());
        payment.setPointsDiscount(discountCalc.getDiscountAmount());
        
        payment.setTradeDate(formatTradeDate(LocalDateTime.now()));
        payment.setCreatedAt(LocalDateTime.now());
        payment.setSimulatePaid(false);      

        paymentRepository.save(payment);
        
        // 5. 如果是會員且使用點數，立即扣除
        if (!isGuest && discountCalc.isUsingPoints()) {
            try {
                pointService.usePointsForPayment(payment, discountCalc.getActualPointsToUse());
                log.info("✅ 點數預扣成功 - 使用: {}點", discountCalc.getActualPointsToUse());
            } catch (Exception e) {
                log.error("❌ 點數預扣失敗", e);
                throw new RuntimeException("點數扣除失敗: " + e.getMessage());
            }
        }

        // 6. 取得會員資訊
        String memberInfo = isGuest ? "訪客用戶" : "會員用戶";
        Integer currentUserPoints = 0;
        
        if (!isGuest) {
            currentUserPoints = pointService.getCurrentUserBalance(request.getPayerUserId());
        }
        
        // 7. 建立綠界參數
        Map<String, String> formParams = buildSimpleEcpayParameters(payment);
        String checkMacValue = calculateCheckMacValue(formParams);
        formParams.put("CheckMacValue", checkMacValue);
        
        // 8. 組裝回傳資料
        return PaymentFormDTO.builder()
                .action("https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5")
                .merchantTradeNo(payment.getMerchantTradeNo())
                .totalAmount(payment.getTotalAmount())
                .itemName("POS系統消費")
                .formData(formParams)
                
                // 金額資訊
                .originalAmount(orderGroup.getTotalAmount())
                .actualPayAmount(discountCalc.getFinalPayAmount())
                
                // 點數使用資訊
                .pointsUsed(discountCalc.getActualPointsToUse())
                .pointsDiscount(discountCalc.getDiscountAmount())
                
                // 會員資訊
                .expectedEarnPoints(discountCalc.getNewEarnPoints())
                .memberInfo(memberInfo)
                .currentUserPoints(currentUserPoints)
                .pointsEarnRule("每10元1點")
                
                .orderTime(orderGroup.getCreatedAt())
                .build();
    }	
    
     // 核心方法 2:處理付款回調 (點數已預扣, 只須累積新點數)
     
    public PaymentResultDTO handlePaymentCallback(Map<String, String> params) {
        
    	 String merchantTradeNo = params.get("MerchantTradeNo");
    	    String rtnCode = params.get("RtnCode");
    	    
    	    log.info("📞 收到付款回調 - MerchantTradeNo: {}, RtnCode: {}", merchantTradeNo, rtnCode);
    	    
    	    Payment payment = paymentRepository.findByMerchantTradeNo(merchantTradeNo)
    	            .orElseThrow(() -> new RuntimeException("找不到付款記錄"));
    	    
    	    boolean isGuest = (payment.getPayerUserId() == null);
    	    
    	    if ("1".equals(rtnCode)) {
    	        // 付款成功
    	        payment.setTradeStatus("SUCCESS");
    	        payment.setPaidAt(LocalDateTime.now());
    	        log.info("✅ 付款成功 - {} ({})", merchantTradeNo, isGuest ? "訪客" : "會員");
    	        
    	        // 自動完成結帳
    	        try {
    	            orderGroupService.completeOrderGroup(payment.getOrderGroupId());
    	            log.info("🎉 訂單群組已自動完成結帳 - OrderGroupId: {}", payment.getOrderGroupId());
    	        } catch (Exception e) {
    	            log.error("❌ 自動完成結帳失敗", e);
    	        }
    	        
    	        // 📧 發送付款成功郵件（只給會員）
    	        if (!isGuest) {
    	            try {
    	                log.info("📧 準備發送付款成功郵件...");
    	                sendPaymentSuccessEmail(payment);
    	                log.info("✅ 郵件發送成功");
    	            } catch (Exception e) {
    	                log.error("❌ 郵件發送失敗: {}", e.getMessage());
    	            }
    	        } else {
    	            log.info("👤 訪客付款，跳過郵件發送");
    	        }
    	        
    	        // 累積新點數（只給會員，基於實付的金額）
    	        if (!isGuest) {
    	            try {
    	                var pointRecord = pointService.earnPointsFromPayment(payment);
    	                if (pointRecord != null) {
    	                    log.info("✨ 新點數累積成功 - 獲得 {} 點 (基於實付金額 NT${})", 
    	                            pointRecord.getPointsAmount(), payment.getTotalAmount());
    	                } else {
    	                    log.info("此次付款不累積點數");
    	                }
    	            } catch (Exception e) {
    	                log.error("❌ 新點數累積失敗", e);
    	            }
    	        } else {
    	            log.info("👤 訪客付款，不累積點數");
    	        }
    	        
    	    } else {
    	        // 付款失敗 - 需要退還已扣除點數（只針對會員）
    	        payment.setTradeStatus("FAILED");
    	        
    	        if (!isGuest && payment.getPointsUsed() != null && payment.getPointsUsed() > 0) {
    	            try {
    	                pointService.grantPointsByAdmin(
    	                        payment.getPayerUserId(),
    	                        payment.getPointsUsed(), 
    	                        "付款失敗退還點數 - " + merchantTradeNo
    	                );
    	                log.info("🔄 付款失敗，已退還點數: {}點", payment.getPointsUsed());
    	            } catch (Exception e) {
    	                log.error("❌ 點數退還失敗", e);
    	            }
    	        }
    	    }
    	    
    	    payment.setRtnCode(rtnCode);
    	    payment.setRtnMsg(params.get("RtnMsg"));
    	    payment.setTradeNo(params.get("TradeNo"));
    	    paymentRepository.save(payment);
    	    
    	    return PaymentResultDTO.builder()
    	            .merchantTradeNo(merchantTradeNo)
    	            .tradeStatus(payment.getTradeStatus())
    	            .success("1".equals(rtnCode))
    	            .message("1".equals(rtnCode) ? "付款成功" : "付款失敗")
    	            .totalAmount(payment.getTotalAmount())
    	            .paidAt(payment.getPaidAt())
    	            .build();
    	}

    	
    /**
     * 🧪 核心方法3：模擬付款
     */
    public PaymentResultDTO simulatePayment(String merchantTradeNo, boolean success) {
        
        Payment payment = paymentRepository.findByMerchantTradeNo(merchantTradeNo)
                .orElseThrow(() -> new RuntimeException("找不到付款記錄"));
        
        // 模擬回調參數
        Map<String, String> params = new HashMap<>();
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("RtnCode", success ? "1" : "0");
        params.put("RtnMsg", success ? "Success" : "Fail");
        params.put("TradeNo", "SIM" + System.currentTimeMillis());
        
        payment.setSimulatePaid(true);
        paymentRepository.save(payment);
        
        return handlePaymentCallback(params);
    }
    
    /**
     *  查詢方法1：根據商家交易編號查詢付款詳情
     */
    public PaymentDetailDTO getPaymentByMerchantTradeNo(String merchantTradeNo) {
        log.info("🔍 查詢付款狀態 - MerchantTradeNo: {}", merchantTradeNo);
        
        Payment payment = paymentRepository.findByMerchantTradeNo(merchantTradeNo)
                .orElseThrow(() -> new RuntimeException("找不到付款記錄: " + merchantTradeNo));
        
        return convertToDetailDTO(payment);
    }

    /**
     *  查詢方法2：根據訂單群組ID查詢付款詳情
     */
    public PaymentDetailDTO getPaymentByOrderGroupId(UUID orderGroupId) {
        log.info("🔍 查詢付款狀態 - OrderGroupId: {}", orderGroupId);
        
        Payment payment = paymentRepository.findByOrderGroupId(orderGroupId)
                .orElseThrow(() -> new RuntimeException("找不到該群組的付款記錄: " + orderGroupId));
        
        return convertToDetailDTO(payment);
    }

    /**
     *  轉換Payment實體為PaymentDetailDTO
     */
    private PaymentDetailDTO convertToDetailDTO(Payment payment) {
        return PaymentDetailDTO.builder()
                .paymentId(payment.getId())
                .merchantTradeNo(payment.getMerchantTradeNo())
                .tradeNo(payment.getTradeNo())
                .orderGroupId(payment.getOrderGroupId())
                .payerUserId(payment.getPayerUserId())
                .totalAmount(payment.getTotalAmount())
                .pointsUsed(payment.getPointsUsed())
                .pointsDiscount(payment.getPointsDiscount())
                .tradeStatus(payment.getTradeStatus())
                .choosePayment(payment.getChoosePayment())
                .tradeDesc(payment.getTradeDesc())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .rtnMsg(payment.getRtnMsg())
                .build();
    }
    
    
    // === 🔧 必要的支援方法 ===
    
    private String generateMerchantTradeNo() {
        return "PAY" + System.currentTimeMillis();
    }
    
    private String formatTradeDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }
    
    private Map<String, String> buildSimpleEcpayParameters(Payment payment) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", payment.getMerchantTradeNo());
        params.put("MerchantTradeDate", payment.getTradeDate());
        params.put("PaymentType", "aio");
        params.put("TotalAmount", payment.getTotalAmount().toString());
        params.put("TradeDesc", "POS系統消費");
        params.put("ItemName", "POS系統消費");
        params.put("ReturnURL", returnUrl);
        params.put("OrderResultURL", callbackUrl);
        params.put("ChoosePayment", "Credit");
        params.put("EncryptType", "1");
        params.put("ClientBackURL", returnUrl);
        return params;
    }
    
    private String calculateCheckMacValue(Map<String, String> params) {
        try {
            Map<String, String> sortedParams = new TreeMap<>(params);
            sortedParams.remove("CheckMacValue");
            
            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(hashKey).append("&");
            
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            
            sb.append("HashIV=").append(hashIV);
            
            String encodedString = java.net.URLEncoder.encode(sb.toString(), "UTF-8")
                    .toLowerCase()
                    .replace("%2d", "-")
                    .replace("%5f", "_")
                    .replace("%2e", ".")
                    .replace("%21", "!")
                    .replace("%2a", "*")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%20", "+");
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(encodedString.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
            
        } catch (Exception e) {
            throw new RuntimeException("CheckMacValue計算失敗", e);
        }
    }
    
    private void sendPaymentSuccessEmail(Payment payment) {
        try {
            log.info("📧 開始準備發送郵件 - MerchantTradeNo: {}", payment.getMerchantTradeNo());
            
            // 1. 獲取訂單群組資訊
            OrderGroup orderGroup = orderGroupRepository.findById(payment.getOrderGroupId())
                    .orElseThrow(() -> new RuntimeException("找不到訂單群組"));
            log.info("✓ 訂單群組查詢成功 - OrderGroupId: {}", orderGroup.getId());
            
            // 2. 獲取付款人資訊
            String memberEmail = null;
            String memberName = "顧客";
            
            if (payment.getPayerUserId() != null) {
                Optional<User> userOpt = userRepository.findById(payment.getPayerUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    memberEmail = user.getEmail();
                    memberName = user.getNickname() != null && !user.getNickname().isEmpty() 
                            ? user.getNickname() 
                            : user.getEmail();
                }
            }
            
            if (memberEmail == null || memberEmail.isEmpty()) {
                log.warn("⚠️ 付款人沒有郵件地址，跳過發送 - UserId: {}", payment.getPayerUserId());
                return;
            }
            log.info("✓ 付款人資訊: {} ({})", memberName, memberEmail);
            
            // 3. 收集所有訂單項目
            List<Orders> allOrders = ordersRepository.findAllOrdersByGroupId(payment.getOrderGroupId());
            List<OrderEmailDTO.OrderItemDTO> allItems = new ArrayList<>();
            
            log.info("✓ 查詢到 {} 筆訂單", allOrders.size());
            
            for (Orders order : allOrders) {
                List<OrderItems> orderItems = orderItemsRepository
                        .findOrderItemsWithProductsByOrderId(order.getId());
                
                for (OrderItems item : orderItems) {
                    // 使用 OrderEmailDTO.OrderItemDTO 內部類別
                    OrderEmailDTO.OrderItemDTO itemDTO = OrderEmailDTO.OrderItemDTO.builder()
                            .productName(item.getProducts().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice().intValue())
                            .subtotal(item.getUnitPrice().multiply(
                                new java.math.BigDecimal(item.getQuantity())).intValue())
                            .note(item.getNote())
                            .build();
                    allItems.add(itemDTO);
                }
            }
            
            log.info("✓ 收集到 {} 個商品項目", allItems.size());
            
            // 4. 組裝郵件DTO（使用正確的欄位名稱）
            OrderEmailDTO emailDTO = OrderEmailDTO.builder()
                    .merchantTradeNo(payment.getMerchantTradeNo())
                    .memberName(memberName)  // 使用 memberName
                    .memberEmail(memberEmail)  // 使用 memberEmail
                    .orderGroupId(orderGroup.getId().toString())
                    .orderDate(orderGroup.getCreatedAt())
                    .tableInfo("桌號 " + orderGroup.getTable().getTableId())
                    .originalAmount(orderGroup.getTotalAmount())
                    .pointsUsed(payment.getPointsUsed() != null ? payment.getPointsUsed() : 0)
                    .pointsDiscount(payment.getPointsDiscount() != null ? payment.getPointsDiscount() : 0)
                    .finalAmount(payment.getTotalAmount())
                    .earnedPoints(0)  // 如果有累積點數，從 PointService 取得
                    .items(allItems)
                    .build();
            
            log.info("✓ 郵件 DTO 組裝完成");
            
            // 5. 發送郵件（使用正確的方法名稱）
            emailService.sendOrderConfirmationEmail(emailDTO);
            log.info("✅ 郵件已發送至: {} (顧客: {})", memberEmail, memberName);
            
        } catch (Exception e) {
            log.error("❌ 郵件發送失敗 - Payment: {}, Error: {}", 
                     payment.getMerchantTradeNo(), e.getMessage(), e);
            // 不拋出異常，避免影響付款流程
        }
    }
}