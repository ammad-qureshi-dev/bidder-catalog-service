package com.bidder.catalog_service.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
public class ExternalServiceProperties {

    @Value("${internal-services.bidding-service}")
    private String biddingServiceUri;

    @Value("${internal-services.identity-and-auth-service}")
    private String identityAndAuthUri;

    @Bean
    @ConfigurationProperties(prefix = "bidding-service")
    public Map<String, String> biddingServiceEndpoints() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "identity-and-auth-service")
    public Map<String, String> identityAndAuthServiceEndpoints() {
        return new HashMap<>();
    }
}
