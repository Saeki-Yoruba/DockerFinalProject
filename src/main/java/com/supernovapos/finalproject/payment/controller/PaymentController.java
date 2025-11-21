package com.supernovapos.finalproject.payment.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.payment.model.dto.CreatePaymentRequestDTO;
import com.supernovapos.finalproject.payment.model.dto.PaymentDetailDTO;
import com.supernovapos.finalproject.payment.model.dto.PaymentFormDTO;
import com.supernovapos.finalproject.payment.model.dto.PaymentResultDTO;
import com.supernovapos.finalproject.payment.service.EcpayService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

	@Autowired
	private EcpayService ecpayService;
	
	private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

	@PostMapping("/create")
	@PermitAll
	@Operation(summary = "建立付款訂單", description = "建立綠界付款表單，用於桌邊點餐結帳流程。會驗證訂單群組、檢查重複付款、計算點數折扣，並回傳綠界付款表單參數")
	public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequestDTO request) {
		try {
			PaymentFormDTO paymentForm = ecpayService.createPaymentForm(request);
			return ResponseEntity.ok(paymentForm);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/simulate/{merchantTradeNo}")
	@PermitAll
	@Operation(summary = "模擬付款結果", description = "測試環境專用：模擬綠界付款回調，可指定成功或失敗，用於開發階段測試付款流程和點數機制")
	public ResponseEntity<?> simulatePayment(@PathVariable String merchantTradeNo,
			@RequestParam(defaultValue = "true") boolean success) {
		try {
			var result = ecpayService.simulatePayment(merchantTradeNo, success);
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@GetMapping("/status/{merchantTradeNo}")
	@PermitAll
	@Operation(summary = "查詢付款狀態", description = "根據商家交易編號查詢付款狀態")
	public ResponseEntity<?> getPaymentStatus(@PathVariable String merchantTradeNo) {
		try {
			PaymentDetailDTO paymentDetail = ecpayService.getPaymentByMerchantTradeNo(merchantTradeNo);
			return ResponseEntity.ok(paymentDetail);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	@GetMapping("/status/group/{orderGroupId}")
	@PermitAll
	@Operation(summary = "根據群組ID查詢付款狀態", description = "根據訂單群組ID查詢付款狀態")
	public ResponseEntity<?> getPaymentStatusByGroupId(@PathVariable UUID orderGroupId) {
		try {
			PaymentDetailDTO paymentDetail = ecpayService.getPaymentByOrderGroupId(orderGroupId);
			return ResponseEntity.ok(paymentDetail);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PostMapping("/callback")
	@PermitAll // 🚨 關鍵：允許綠界無認證訪問
	@Operation(summary = "付款回調", description = "接收綠界的付款回調通知")
	public ResponseEntity<String> paymentCallback(@RequestParam java.util.Map<String, String> params) {
		 try {
		        log.info("收到綠界回調: {}", params);
		        PaymentResultDTO result = ecpayService.handlePaymentCallback(params);
		        
		        // 重定向到前端結果頁面
		        String redirectUrl = "http://192.168.38.69:5173/payment/result?merchantTradeNo=" + 
		                           params.get("MerchantTradeNo") + "&status=" + result.getTradeStatus();
		        
		        String html = "<script>window.location.href='" + redirectUrl + "';</script>";
		        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
		        
		    } catch (Exception e) {
		        log.error("處理回調失敗", e);
		        return ResponseEntity.ok("0|ERROR");
		    }
	}
}
