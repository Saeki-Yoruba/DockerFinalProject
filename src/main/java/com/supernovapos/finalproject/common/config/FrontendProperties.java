package com.supernovapos.finalproject.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {
    private String baseUrl;
}
