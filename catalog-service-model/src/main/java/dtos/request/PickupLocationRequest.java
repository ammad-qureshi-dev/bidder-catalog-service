package dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record PickupLocationRequest(@NotNull Long unit,

		@NotBlank String street,

		@NotBlank String city,

		@NotBlank String postalCode,

		@NotBlank String state,

		@NotBlank String country,

		@Length(max = 512) String instructions) {
}
