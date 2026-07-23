package dtos.response.summary;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemSummaryResponse(UUID id, UUID auctionId, String title, String description, BigDecimal minimumPrice,
		boolean sold) {
}
