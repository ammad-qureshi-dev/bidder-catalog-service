package dtos.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record BiddingItemRequest(@NotNull @Length(max = 128) String title,

		@Length(max = 512) String description,

		@DecimalMin("0.0") BigDecimal minimumPrice,

		UUID id) {
}
