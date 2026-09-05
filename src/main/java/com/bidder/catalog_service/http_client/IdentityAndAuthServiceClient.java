/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.http_client;

import java.util.*;
import java.util.stream.Collectors;

import com.bidder.catalog_service.config.ExternalServiceProperties;
import com.bidder.catalog_service.config.IdentityAndAuthServiceEndpoints;
import dtos.response.AppUserDto;
import dtos.response.PreferredContactMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import response.ApiResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityAndAuthServiceClient {

	private final RestClient restClient;

	private final ExternalServiceProperties props;

	private final IdentityAndAuthServiceEndpoints endpoints;

	/**
	 * Internal endpoint; retrieves the preferred contacts of all appUserIds
	 * provided
	 * 
	 * @param appUserIds
	 *            all requested app user ids
	 */
	public List<PreferredContactMethod> internalGetPreferredContactMethods(List<UUID> appUserIds) {
		if (appUserIds == null || appUserIds.isEmpty()) {
			return Collections.emptyList();
		}

		var baseUri = props.getIdentityAndAuthUri();
		var ids = appUserIds.stream().map(UUID::toString).collect(Collectors.joining(","));

		var url = UriComponentsBuilder.fromUriString(baseUri).path(endpoints.getPreferredContactMethods())
				.queryParam("appUserIds", ids).build().toUriString();

		try {
			log.info("Calling {}", url);
			var response = restClient.get().uri(url).retrieve()
					.body(new ParameterizedTypeReference<ApiResponse<List<PreferredContactMethod>>>() {
					});

			if (response == null) {
				log.error("Response was null for request {}", url);
				throw new RuntimeException("No response received from identity-and-auth-service. Please check logs");
			}

			return response.getData();
		} catch (RuntimeException e) {
			log.error("Error on internalGetPreferredContactMethods: ", e);
			// throw new NoSuchElementException("Item not found");
		}

		return Collections.emptyList();
	}

	public Optional<AppUserDto> getAppUserById(UUID appUserId) {
		if (appUserId == null) {
			throw new RuntimeException("Np app-user-id provided");
		}

		var baseUri = props.getIdentityAndAuthUri();

		var url = UriComponentsBuilder.fromUriString(baseUri).path(endpoints.getPreferredContactMethods())
				.pathSegment(appUserId.toString()).build().toUriString();

		try {
			log.info("Calling {}", url);
			var response = restClient.get().uri(url).retrieve()
					.body(new ParameterizedTypeReference<ApiResponse<AppUserDto>>() {
					});

			if (response == null) {
				log.error("Response was null for request {}", url);
				throw new RuntimeException("No response received from identity-and-auth-service. Please check logs");
			}

			return Optional.of(response.getData());
		} catch (RuntimeException e) {
			log.error("Error on getAppUserById: ", e);
			// throw new NoSuchElementException("Item not found");
		}

		return Optional.empty();
	}

}
