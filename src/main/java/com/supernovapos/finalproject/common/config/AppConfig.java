package com.supernovapos.finalproject.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class AppConfig {
	/**
     * 全域 PasswordEncoder
     * 可以幫密碼加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 全域 ObjectMapper
     * 可以在這裡設定日期格式、忽略未知屬性等等
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())   // 支援 Java 8 時間類型
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 全域 RestClient
     * 可配置連線 Timeout / BaseUrl / 攔截器等
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                //.baseUrl("https://api.example.com") // 如果有預設 base URL 可以加
                .build();
    }
    
    /**
    * 建立全域 RestTemplate Bean
    *
    * RestTemplate 是 Spring 提供的同步 HTTP 客戶端工具，可以用來向外部 API 發送 HTTP 請求（GET、POST、PUT、DELETE…）。
    * 將它宣告為 @Bean 後，Spring 會把它交給容器管理，讓你可以在任何需要的地方用 @Autowired 注入使用。
    * 
    * 例如：在 Controller 或 Service 內發送 Discord Webhook、呼叫其他後端 API 等。
    */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

 
    
    
}