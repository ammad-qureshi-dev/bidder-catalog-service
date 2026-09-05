/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.controllers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bidder.catalog_service.services.AuctionService;
import dtos.request.AuctionRequest;
import dtos.response.AuctionResponse;
import dtos.response.summary.AuctionSummaryResponse;
import dtos.response.summary.ItemSummaryResponse;
import enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import response.ApiResponse;

@RestController
@RequestMapping("/api/v1/auction")
@RequiredArgsConstructor
public class AuctionController {

	private final AuctionService auctionService;

	@PostMapping
	public ResponseEntity<ApiResponse<UUID>> createAuction(@RequestBody AuctionRequest request,
			@RequestHeader("X-App-User-Id") UUID appUserId) {
		var auctionId = auctionService.createAuction(request, appUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<UUID>builder().data(auctionId).build());
	}

	@PutMapping("/{auctionId}")
	public ResponseEntity<ApiResponse<UUID>> updateAuction(@PathVariable UUID auctionId,
			@RequestBody AuctionRequest request) {
		var response = auctionService.updateAuction(auctionId, request);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<UUID>builder().data(response).build());
	}

	@PutMapping("/{auctionId}/status/{status}")
	public ResponseEntity<ApiResponse<UUID>> updateAuctionStatus(@PathVariable UUID auctionId,
			@PathVariable AuctionStatus status) {
		auctionService.updateAuctionStatus(auctionId, status);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<UUID>builder().data(auctionId).build());
	}

	@GetMapping("/{auctionId}")
	public ResponseEntity<ApiResponse<AuctionResponse>> getAuction(@PathVariable UUID auctionId) {
		var response = auctionService.getAuctionResponse(auctionId);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<AuctionResponse>builder().data(response).build());
	}

	@GetMapping("/{auctionId}/items")
	public ResponseEntity<ApiResponse<List<ItemSummaryResponse>>> getAuctionItems(@PathVariable UUID auctionId) {
		var response = auctionService.getItemsInAuction(auctionId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.<List<ItemSummaryResponse>>builder().data(response).build());
	}

	@GetMapping("/my-auctions")
	public ResponseEntity<ApiResponse<List<AuctionSummaryResponse>>> getMyAuctions(
			@RequestHeader("X-App-User-Id") UUID appUserId) {
		var response = auctionService.getMyAuctions(appUserId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.<List<AuctionSummaryResponse>>builder().data(response).build());
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<AuctionSummaryResponse>>> searchAuctions(
			@RequestParam(required = false) String title, @RequestParam(required = false) AuctionStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAfter,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endBefore) {
		var results = auctionService.searchAuctions(title, status, startAfter, endBefore);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.<List<AuctionSummaryResponse>>builder().data(results).build());
	}

}
