/* (C) 2026
bidder.app */
package com.bidder.catalog_service.controllers;

import java.util.UUID;

import com.bidder.catalog_service.models.response.ApiResponse;
import com.bidder.catalog_service.services.ItemService;
import lombok.RequiredArgsConstructor;
import dtos.response.ItemResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import static com.bidder.catalog_service.utils.Constants.Controller.BASE_URI;
import static com.bidder.catalog_service.utils.Constants.Controller.V1;

@RestController
@RequestMapping(BASE_URI + V1 + "/item")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping("/{itemId}")
	public ResponseEntity<ApiResponse<ItemResponse>> getItem(@PathVariable UUID itemId) {
		var item = itemService.getItem(itemId);
		return ResponseEntity.ok().body(ApiResponse.<ItemResponse>builder().data(item).build());
	}
}
