package dtos.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import enums.AuctionCategory;

public record AuctionRequest(@NotNull String title,

		@NotNull LocalDateTime startTime,

		@NotNull LocalDateTime endTime,

		List<BiddingItemRequest> biddingItems,

		List<AuctionCategory> categories) {
}
