package dtos.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdatedBidRequest(
        @NotNull UUID auctionId,
        @NotNull UUID itemId,
        @NotNull UUID bidderId,
        @NotNull UUID bidId,
        @NotNull BigDecimal amount
        ) {
}
