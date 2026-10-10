/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PickupLocation {

	@NotNull private Long unit;

	@NotBlank
	private String street;

	@NotBlank
	private String city;

	@NotBlank
	private String postalCode;

	@NotBlank
	private String state;

	@NotBlank
	private String country;

	// optional free-text pickup instructions
	@Length(max = 512) private String instructions;
}
