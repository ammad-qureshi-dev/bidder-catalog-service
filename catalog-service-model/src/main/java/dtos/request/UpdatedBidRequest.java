package dtos.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdatedBidRequest(
        @NotNull UUID auctionId,
        @NotNull UUID itemId,

        UUID bidderId,
        UUID bidId,
        BigDecimal amount,

        // ToDo: set the previousBidAmount when a bid gets rejected
        BigDecimal prevBidAmount
        ) {
}
