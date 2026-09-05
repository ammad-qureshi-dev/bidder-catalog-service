/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.controllers;

import java.util.UUID;

import com.bidder.catalog_service.services.ItemService;
import dtos.request.UpdatedBidRequest;
import dtos.response.ItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import response.ApiResponse;

@RestController
@RequestMapping("/api/v1/item")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping("/{itemId}")
	public ResponseEntity<ApiResponse<ItemResponse>> getItem(@PathVariable UUID itemId) {
		var item = itemService.getItem(itemId);
		return ResponseEntity.ok().body(ApiResponse.<ItemResponse>builder().data(item).build());
	}

	@Operation(description = "Updates the item's highest bid with the provided request. Returns the old, outbid bid")
	@PutMapping("/{itemId}/update-highest-bid")
	public ResponseEntity<ApiResponse<UUID>> updateHighestBid(@PathVariable UUID itemId,
			@RequestBody UpdatedBidRequest request) {
		var outbidBidId = itemService.updateHighestBidForItem(itemId, request);
		return ResponseEntity.ok().body(ApiResponse.<UUID>builder().data(outbidBidId).build());
	}
}
