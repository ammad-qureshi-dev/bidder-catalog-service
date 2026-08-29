/* (C) 2026
bidder.app */
package com.bidder.catalog_service.utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Constants {

	@AllArgsConstructor
	public static class Controller {
		public static final String BASE_URI = "/api/catalog";
	}

	public static class Messages {
		public static final String AUCTION_CLOSED = "Cannot perform action -- this auction is closed";
		public static final String AUCTION_PAUSED = "Cannot perform action -- this auction is paused";
	}

	public static class Database {
		public static final String SCHEMA = "catalog_service";
	}
}
