/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.controllers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bidder.catalog_service.services.AuctionService;
import dtos.request.AuctionRequest;
import dtos.response.AuctionResponse;
import dtos.response.PageResponse;
import dtos.response.summary.AuctionSummaryResponse;
import dtos.response.summary.ItemSummaryResponse;
import enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

	// ToDo: [SECURITY CONCERN] -- X-App-User-Id is a plain client-supplied header,
	// not a value derived from a
	// verified session/JWT. Since SecurityConfig permits all requests, any caller
	// can set this header to an
	// arbitrary UUID and create auctions (or, on other endpoints, read/act) as any
	// user.
	// ToDo: [SECURITY CONCERN] -- @RequestBody is not annotated with @Valid, so the
	// @NotNull constraints declared
	// on AuctionRequest are never enforced, allowing malformed/null input to reach
	// the service layer.
	@PostMapping
	public ResponseEntity<ApiResponse<UUID>> createAuction(@RequestBody AuctionRequest request,
			@RequestHeader("X-App-User-Id") UUID appUserId) {
		var auctionId = auctionService.createAuction(request, appUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<UUID>builder().data(auctionId).build());
	}

	// ToDo: [SECURITY CONCERN] -- no ownership/authorization check: this endpoint
	// does not verify that the caller
	// is the auction's owner before applying the update. Combined with no
	// authentication, any user can edit any
	// other user's auction (IDOR).
	@PutMapping("/{auctionId}")
	public ResponseEntity<ApiResponse<UUID>> updateAuction(@PathVariable UUID auctionId,
			@RequestBody AuctionRequest request) {
		var response = auctionService.updateAuction(auctionId, request);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<UUID>builder().data(response).build());
	}

	// ToDo: [SECURITY CONCERN] -- no ownership/authorization check: any caller can
	// transition any auction's status
	// (e.g. force-close, pause, or re-open) regardless of whether they own it,
	// since there is no check against the
	// caller's identity here.
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

	// ToDo: paginated
	@GetMapping("/{auctionId}/items")
	public ResponseEntity<ApiResponse<List<ItemSummaryResponse>>> getAuctionItems(@PathVariable UUID auctionId) {
		var response = auctionService.getItemsInAuction(auctionId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.<List<ItemSummaryResponse>>builder().data(response).build());
	}

	// ToDo: [SECURITY CONCERN] -- same unverified X-App-User-Id trust issue as
	// createAuction: a caller can pass
	// any user's id here and retrieve that user's auction list.
	@GetMapping("/my-auctions")
	public ResponseEntity<ApiResponse<PageResponse<AuctionSummaryResponse>>> getMyAuctions(
			@RequestHeader("X-App-User-Id") UUID appUserId,
			@PageableDefault(size = 25, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		var response = auctionService.getMyAuctions(appUserId, pageable);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.<PageResponse<AuctionSummaryResponse>>builder().data(response).build());
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<PageResponse<AuctionSummaryResponse>>> searchAuctions(
			@RequestParam(required = false) String title, @RequestParam(required = false) AuctionStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAfter,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endBefore,
			@PageableDefault(size = 25, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
		var results = auctionService.searchAuctions(title, status, startAfter, endBefore, pageable);
		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.<PageResponse<AuctionSummaryResponse>>builder().data(results).build());
	}

}
