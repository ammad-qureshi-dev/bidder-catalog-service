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
import models.entities.BidStatus;
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

		var bidIdsQueryParam = generateListQueryParam(bidIds);
		if (bidIdsQueryParam.isEmpty()) {
			return Collections.emptyList();
		}

		var url = UriComponentsBuilder.fromUriString(props.getBiddingServiceUri()).path(endpoints.search())
				.queryParam("bidIds", bidIdsQueryParam).build().toUriString();

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

	public void bulkUpdateBidsStatuses(List<UUID> bidIds, BidStatus status) {
		if (bidIds == null || bidIds.isEmpty()) {
			log.info("No bidIds provided for bulkUpdateBidsStatuses");
			return;
		}

		var bidIdsQueryParam = generateListQueryParam(bidIds);
		if (bidIdsQueryParam.isEmpty()) {
			return;
		}

		var url = UriComponentsBuilder.fromUriString(props.getBiddingServiceUri())
				.path(endpoints.search())
				.queryParam("bidIds", bidIdsQueryParam)
				.buildAndExpand(status)
				.toUriString();

		log.info("Calling {}", url);
		var response = restClient.get().uri(url).retrieve()
				.body(new ParameterizedTypeReference<ApiResponse<List<BidSummaryResponse>>>() {
				});

		if (response == null) {
			log.error("Response was null for request {}", url);
			throw new RuntimeException(
					"No response received from bidding-service. Please check logs");
		}
	}

	private static String generateListQueryParam(List<?> items) {
		if (items == null || items.isEmpty()) {
			return "";
		}

		return items.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(","));
	}
}
