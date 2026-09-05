package dtos.response;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import enums.AuctionCategory;
import enums.AuctionStatus;

public record AuctionResponse(UUID id, UUID ownerId, String title, AuctionStatus auctionStatus,
		List<AuctionCategory> categories, List<ItemResponse> items, Instant startTime, Instant endTime) {
}
