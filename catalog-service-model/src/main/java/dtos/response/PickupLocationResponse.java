package dtos.response;

public record PickupLocationResponse(Long unit, String street, String city, String postalCode, String state,
		String country, String instructions) {
}
