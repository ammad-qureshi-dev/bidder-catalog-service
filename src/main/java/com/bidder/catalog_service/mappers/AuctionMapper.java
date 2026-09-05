/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.mappers;

import com.bidder.catalog_service.models.Auction;
import dtos.request.AuctionRequest;
import dtos.response.AuctionResponse;
import dtos.response.summary.AuctionSummaryResponse;

public class AuctionMapper {

	public static Auction requestToEntity(AuctionRequest request) {
		return Auction.builder().title(request.title()).startTime(request.startTime()).endTime(request.endTime())
				.items(request.biddingItems().stream().map(ItemMapper::requestToEntity).toList())
				.categories(request.categories()).build();
	}

	public static AuctionResponse entityToResponse(Auction entity) {
		var itemsDto = entity.getItems().stream().map(ItemMapper::entityToResponse).toList();
		return new AuctionResponse(entity.getId(), entity.getOwnerId(), entity.getTitle(), entity.getAuctionStatus(),
				entity.getCategories(), itemsDto, entity.getStartTime(), entity.getEndTime());
	}

	public static AuctionSummaryResponse entityToSummary(Auction a) {
		return new AuctionSummaryResponse(a.getId(), a.getTitle(), a.getAuctionStatus(), a.getCategories(),
				a.getItems().size(), a.getStartTime(), a.getEndTime());
	}

}
