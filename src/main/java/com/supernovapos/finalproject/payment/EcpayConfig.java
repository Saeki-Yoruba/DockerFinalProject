//package com.supernovapos.finalproject.payment;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//import lombok.Data;
//
//@ConfigurationProperties(prefix = "ecpay.test")
//@Data
//@Component
//public class EcpayConfig {
//    private String merchantId;
//    private String hashKey;
//    private String hashIv;
//    private String apiUrl;
//    private String returnUrl;
//    private String orderResultUrl;
//    private String clientBackUrl;
//}

package com.supernovapos.finalproject.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "ecpay")
@Data
public class EcpayConfig {
    
    /**
     * API相關設定
     */
    private Api api = new Api();
    
    /**
     * 商家相關設定
     */
    private Merchant merchant = new Merchant();
    
    /**
     * Hash加密相關設定
     */
    private Hash hash = new Hash();
    
    @Data
    public static class Api {
        private String host;
        private Aio aio = new Aio();
        
        @Data
        public static class Aio {
            private String url;
        }
    }
    
    @Data
    public static class Merchant {
        private String id;
        private String name;
    }
    
    @Data
    public static class Hash {
        private String key;
        private String iv;
    }
}