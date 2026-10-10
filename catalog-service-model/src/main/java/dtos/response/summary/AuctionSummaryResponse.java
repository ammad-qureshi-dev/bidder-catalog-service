package dtos.response.summary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import dtos.response.PickupLocationResponse;
import enums.AuctionCategory;
import enums.AuctionStatus;

public record AuctionSummaryResponse(UUID id, String title, AuctionStatus status, List<AuctionCategory> categories,
									 int itemCount, Instant startTime, Instant endTime, PickupLocationResponse pickupLocation) {
}
