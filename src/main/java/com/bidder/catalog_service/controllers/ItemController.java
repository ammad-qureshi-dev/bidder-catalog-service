/* (C) 2026
bidder.app */
package com.bidder.catalog_service.controllers;

import java.util.UUID;

import com.bidder.catalog_service.models.response.ApiResponse;
import com.bidder.catalog_service.services.ItemService;
import lombok.RequiredArgsConstructor;
import dtos.response.ItemResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import static com.bidder.catalog_service.utils.Constants.Controller.*;

@RestController
@RequestMapping(BASE_URI_V1 + "/item")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping("/{itemId}")
	public ResponseEntity<ApiResponse<ItemResponse>> getItem(@PathVariable UUID itemId) {
		var item = itemService.getItem(itemId);
		return ResponseEntity.ok().body(ApiResponse.<ItemResponse>builder().data(item).build());
	}
}
