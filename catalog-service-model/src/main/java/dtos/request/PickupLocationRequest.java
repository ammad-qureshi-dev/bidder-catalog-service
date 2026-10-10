package dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PickupLocationRequest(@NotNull Long unit,

		@NotBlank String street,

		@NotBlank String city,

		@NotBlank String postalCode,

		@NotBlank String state,

		@NotBlank String country) {
}
