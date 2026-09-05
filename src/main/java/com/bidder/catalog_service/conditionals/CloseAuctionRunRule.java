/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.conditionals;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CloseAuctionRunRule implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		String enabled = context.getEnvironment().getProperty("run-close-auction-schedule-job");
		return Boolean.parseBoolean(enabled);
	}
}
