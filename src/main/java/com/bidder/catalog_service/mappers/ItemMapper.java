/* (C) 2026
bidder.app */
package com.bidder.catalog_service.mappers;

import dtos.request.BiddingItemRequest;
import dtos.response.ItemResponse;
import dtos.response.summary.ItemSummaryResponse;
import com.bidder.catalog_service.models.Item;

public class ItemMapper {

	public static Item requestToEntity(BiddingItemRequest request) {
		return Item.builder().title(request.title()).description(request.description())
				.minimumPrice(request.minimumPrice()).build();
	}

	public static ItemResponse entityToResponse(Item entity) {

		if (entity == null) {
			return null;
		}

		return new ItemResponse(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getMinimumPrice(),
				entity.getPriceSoldAt());
	}

	public static ItemSummaryResponse entityToSummary(Item i) {
		if (i == null) {
			return null;
		}

		var sold = i.getPriceSoldAt() != null;
		return new ItemSummaryResponse(i.getId(), i.getAuction().getId(), i.getTitle(), i.getDescription(),
				i.getMinimumPrice(), sold);
	}
}
