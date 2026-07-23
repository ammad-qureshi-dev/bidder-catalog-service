/* (C) 2026
bidder.app */
package com.bidder.catalog_service.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.bidder.catalog_service.mappers.ItemMapper;
import com.bidder.catalog_service.repository.AuctionRepository;
import com.bidder.catalog_service.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import dtos.request.BiddingItemRequest;
import dtos.response.ItemResponse;
import com.bidder.catalog_service.models.Auction;
import com.bidder.catalog_service.models.Item;
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
}
