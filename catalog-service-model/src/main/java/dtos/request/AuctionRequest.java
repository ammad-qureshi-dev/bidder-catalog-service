package dtos.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import enums.AuctionCategory;

public record AuctionRequest(@NotNull String title,

		@NotNull Instant startTime,

		@NotNull Instant endTime,

		List<BiddingItemRequest> biddingItems,

		List<AuctionCategory> categories) {
}
