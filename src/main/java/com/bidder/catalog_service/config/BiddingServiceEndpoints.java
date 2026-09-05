/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bidding-service")
public record BiddingServiceEndpoints(String search) {
}
