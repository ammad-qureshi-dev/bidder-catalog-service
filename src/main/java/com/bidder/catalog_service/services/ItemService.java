/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.services;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.bidder.catalog_service.mappers.ItemMapper;
import com.bidder.catalog_service.models.Auction;
import com.bidder.catalog_service.models.Item;
import com.bidder.catalog_service.repository.AuctionRepository;
import com.bidder.catalog_service.repository.ItemRepository;
import dtos.request.BiddingItemRequest;
import dtos.request.UpdatedBidRequest;
import dtos.response.ItemResponse;
import enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;
	private final AuctionRepository auctionRepository;

	public void createItems(List<BiddingItemRequest> itemsRequest, Auction auction) {

		final var items = itemsRequest.stream().map(ItemMapper::requestToEntity).toList();

		items.forEach(e -> e.setAuction(auction));
		itemRepository.saveAll(items);
	}

	public ItemResponse getItem(UUID id) {
		var item = getItemById(id);
		return ItemMapper.entityToResponse(item);
	}

	public Item getItemById(UUID id) {
		return itemRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Item not found"));
	}

	public UUID updateHighestBidForItem(UUID itemId, UpdatedBidRequest request) {
		var item = getItemById(itemId);
		var oldBidId = item.getHighestBidId();

		validateBidRequest(request, item);
		validateAuctionRequest(item.getAuction(), request);

		item.setHighestBidId(request.bidId());
		item.setHighestBidAmount(request.amount());

		itemRepository.save(item);

		return oldBidId;
	}

	private static void validateAuctionRequest(Auction auction, UpdatedBidRequest request) {
		if (request.bidderId().equals(auction.getOwnerId())) {
			throw new IllegalStateException("Auction owner cannot place bid on their own auction");
		}

		if (!auction.getAuctionStatus().equals(AuctionStatus.LIVE)) {
			throw new IllegalStateException("Cannot place bid, the auction is " + auction.getAuctionStatus());
		}

		var now = Instant.now();
		if (now.isBefore(auction.getStartTime()) || now.isAfter(auction.getEndTime())) {
			throw new IllegalStateException(
					"Cannot place bid, the bid is placed out of the range of the auction timing");
		}
	}

	private static void validateBidRequest(UpdatedBidRequest request, Item item) {
		if (!isHighestBid(request, item)) {
			throw new IllegalStateException("Bid amount must be higher than the current highest bid");
		} else if (!isAboveMinimumPrice(request, item)) {
			throw new IllegalStateException("Bid amount must be higher than the minimum price on the item");
		} else if (!request.itemId().equals(item.getId())) {
			throw new IllegalStateException("Bid request item does not match item");
		}
	}

	private static boolean isHighestBid(UpdatedBidRequest request, Item item) {
		if (item.getHighestBidId() == null || item.getHighestBidAmount() == null) {
			return true;
		}

		return item.getHighestBidAmount().compareTo(request.amount()) < 0;
	}

	private static boolean isAboveMinimumPrice(UpdatedBidRequest request, Item item) {
		if (item.getMinimumPrice() == null) {
			return request.amount() != null;
		}

		return request.amount().compareTo(item.getMinimumPrice()) > -1;
	}
}
