package com.supernovapos.finalproject.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.supernovapos.finalproject.payment.model.dto.OrderEmailDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private JavaMailSender mailSender;

	@Autowired(required = false)
	private TemplateEngine templateEngine;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${app.name:SuperNova POS}")
	private String appName;

	// 發送訂單確認郵件

	public void sendOrderConfirmationEmail(OrderEmailDTO orderData) {
		try {
			log.info("準備發送訂單確認郵件 - 收件人: {}, 訂單: {}", orderData.getMemberEmail(), orderData.getMerchantTradeNo());

			if (templateEngine != null) {

				sendHtmlOrderEmail(orderData);
			} else {

//				sendPlainTextOrderEmail(orderData);
			}
			log.info("訂單確認郵件發送成功 - {}", orderData.getMerchantTradeNo());
		} catch (Exception e) {
			log.error("發送訂單確認郵件失敗 - {}: {}", orderData.getMerchantTradeNo(), e.getMessage(), e);
			throw new RuntimeException("郵件發送失敗", e);
		}
	}

	private void sendHtmlOrderEmail(OrderEmailDTO orderData) throws MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(fromEmail);
		helper.setTo(orderData.getMemberEmail());
		helper.setSubject(String.format("[%s] 訂單確認通知 - %s", appName, orderData.getMerchantTradeNo()));

		// 模板變數

		Context context = new Context();
		context.setVariable("order", orderData);
		context.setVariable("appName", appName);

		// 渲染 HTML 內容

		String htmlContent = templateEngine.process("order-confirmation", context);
		helper.setText(htmlContent, true);

		mailSender.send(message);
	}

	// 發送純文字格式郵件
	private void sendPlainTextOrderEmail(OrderEmailDTO orderData) {
		SimpleMailMessage message = new SimpleMailMessage();
		
		message.setFrom(fromEmail);
		message.setTo(orderData.getMemberEmail());
		message.setSubject(String.format("[%s] 訂單確認通知 - %s", appName, orderData.getMerchantTradeNo()));
		
		String content = buildPlainTextOrderContent(orderData);
		message.setText(content);
		
		mailSender.send(message);

	}
	
	// 建立純文字郵件內容
	private String buildPlainTextOrderContent(OrderEmailDTO orderData) {

		StringBuilder content = new StringBuilder();
		content.append("=== ").append(appName).append(" 訂單確認通知 ===\n\n");

		// 會員資訊
		content.append("親愛的 ").append(orderData.getMemberName()).append(" 您好，\n\n");
		content.append("感謝您的消費！以下是您的訂單明細：\n\n");

		// 訂單基本資訊
		content.append("訂單編號 :").append(orderData.getMerchantTradeNo()).append("\n");
		content.append("訂單時間 :").append(orderData.getOrderDate()).append("\n");
		
		if(orderData.getTableInfo() !=null ) {
			content.append("用餐桌號 :").append(orderData.getTableInfo()).append("\n");
		}
		content.append("\n");
		
		// 訂單項目
		content.append("=== 訂購項目 ===\n");
		for(OrderEmailDTO.OrderItemDTO item : orderData.getItems()) {
			content.append(String.format("・%s x %d = %s\n",
				item.getProductName(),
				item.getQuantity(),
				item.getFormattedSubtotal()));
			
			if(item.getNote() != null && !item.getNote().isEmpty()) {
				content.append(" 備註:").append(item.getNote()).append("\n");
			}
		}
		content.append("\n");
		
		// 金額明細
		content.append("=== 金額明細 ===\n");
		content.append("訂單金額：").append(orderData.getFormattedOriginalAmount()).append("\n");
		
		if(orderData.hasPointsUsed()) {
			content.append("點數折抵：").append(orderData.getFormattedPointsDiscount())
			.append(" (").append(orderData.getPointsUsed()).append("點)\n");
		}
		
		content.append("實付金額 :").append(orderData.getFormattedFinalAmount()).append("\n");
		
		if(orderData.hasEarnedPoints()) {
			content.append("本次獲得：").append(orderData.getEarnedPoints()).append("點\n");
		}
		
		content.append("\n");
		
		// 結尾
		content.append("感謝您選擇 ").append(appName).append("！\n");
		content.append("如有任何問題，請聯絡客服。\n\n");
		content.append("此為系統自動發送郵件，請勿直接回覆。\n");
	
		return content.toString();
	}
	
	
	// 測試郵件發送功能
	
     
    public void sendTestEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[測試] 郵件發送測試");
            message.setText("這是一封測試郵件，如果您收到此信，表示郵件服務設定正常。");
            
            mailSender.send(message);
            log.info("測試郵件發送成功 - 收件人: {}", toEmail);
            
        } catch (Exception e) {
            log.error("測試郵件發送失敗: {}", e.getMessage(), e);
            throw new RuntimeException("測試郵件發送失敗", e);
        }
    }
}
