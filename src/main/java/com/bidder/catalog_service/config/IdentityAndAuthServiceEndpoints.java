package com.bidder.catalog_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity-and-auth-service")
public record IdentityAndAuthServiceEndpoints(
        String getPreferredContactMethods,
        String getUserById
) {
}
