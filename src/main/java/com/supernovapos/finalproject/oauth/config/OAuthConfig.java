package com.supernovapos.finalproject.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.supernovapos.finalproject.common.config.FrontendProperties;
import com.supernovapos.finalproject.common.util.OAuthRedirectUtil;

@Configuration
public class OAuthConfig {
    @Bean
    public OAuthRedirectUtil oAuthRedirectUtil(FrontendProperties frontendProperties) {
        return new OAuthRedirectUtil(frontendProperties.getBaseUrl());
    }
}
