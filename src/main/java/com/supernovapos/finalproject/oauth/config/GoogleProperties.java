package com.supernovapos.finalproject.oauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "google")
public class GoogleProperties {
    private String authUrl;
    private String tokenUrl;
    private String userinfoUrl;
    private String clientId;
    private String secret;
    private String redirect;
    private String scope;
}