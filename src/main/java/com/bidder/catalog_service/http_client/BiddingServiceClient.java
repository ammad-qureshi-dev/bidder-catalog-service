/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.http_client;

import java.util.*;
import java.util.stream.Collectors;

import com.bidder.catalog_service.config.BiddingServiceEndpoints;
import com.bidder.catalog_service.config.ExternalServiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import models.dtos.response.summary.BidSummaryResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import response.ApiResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiddingServiceClient {

	private final RestClient restClient;

	private final ExternalServiceProperties props;

	private final BiddingServiceEndpoints endpoints;

	public List<BidSummaryResponse> searchByBidIds(List<UUID> bidIds) {
		if (bidIds == null || bidIds.isEmpty()) {
			return Collections.emptyList();
		}

		var baseUri = props.getBiddingServiceUri();
		var ids = bidIds.stream().map(UUID::toString).collect(Collectors.joining(","));

		var url = UriComponentsBuilder.fromUriString(baseUri).path(endpoints.search()).queryParam("bidIds", ids).build()
				.toUriString();

		try {
			log.info("Calling {}", url);
			var response = restClient.get().uri(url).retrieve()
					.body(new ParameterizedTypeReference<ApiResponse<List<BidSummaryResponse>>>() {
					});

			if (response == null) {
				log.error("Response was null for request {}", url);
				throw new RuntimeException(
						"No response received from bidding-service. Bid(s) not found. Please check logs");
			}

			return response.getData();
		} catch (RuntimeException e) {
			log.error("Error on searchByBidIds: ", e);
			throw new NoSuchElementException("Item not found");
		}
	}

}
