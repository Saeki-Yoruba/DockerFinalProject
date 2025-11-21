package com.supernovapos.finalproject.auth.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.supernovapos.finalproject.auth.service.EmailService;
import com.supernovapos.finalproject.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StoreRepository storeRepository;

    // 前端驗證頁面 URL（可以抽到 application.properties）
    @Value("${app.frontend.verify-url:http://192.168.38.69:5173/verify}")
    private String frontendVerifyUrl;

    @Override
	public void sendVerificationEmail(String to, String token) {
        String link = frontendVerifyUrl + "?token=" + token;
        String storeName = storeRepository.findById(1).get().getName();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(storeName + " : 請驗證您的信箱");
        message.setText("請點擊以下連結以驗證您的帳號\n" + link);
        mailSender.send(message);
    }
}

