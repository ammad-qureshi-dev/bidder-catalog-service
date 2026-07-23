/* (C) 2026
bidder.app */
package com.bidder.catalog_service.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bidder.catalog_service.mappers.AuctionMapper;
import com.bidder.catalog_service.mappers.ItemMapper;
import com.bidder.catalog_service.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import dtos.request.AuctionRequest;
import dtos.response.AuctionResponse;
import dtos.response.summary.AuctionSummaryResponse;
import dtos.response.summary.ItemSummaryResponse;
import com.bidder.catalog_service.models.Auction;
import enums.AuctionStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {

	private final AuctionRepository auctionRepository;
	private final ItemService itemService;

	public UUID createAuction(AuctionRequest request, UUID appUserId) {
		validationAuctionRequest(request);

		final var auction = AuctionMapper.requestToEntity(request);
		auction.setOwnerId(appUserId);

		auctionRepository.save(auction);

		itemService.createItems(request.biddingItems(), auction);

		return auction.getId();
	}

	public UUID updateAuction(UUID auctionId, AuctionRequest request) {
		validationAuctionRequest(request);

		final var auction = auctionRepository.findById(auctionId).orElseThrow();
		auction.setTitle(request.title());
		auction.setStartTime(request.startTime());
		auction.setEndTime(request.endTime());
		auction.setCategories(request.categories());

		final var newItems = request.biddingItems().stream().filter(e -> e.id() == null).toList().stream()
				.map(ItemMapper::requestToEntity).toList();

		newItems.forEach(i -> i.setAuction(auction));

		auction.getItems().addAll(newItems);

		auctionRepository.save(auction);

		return auction.getId();
	}

	public Auction getAuctionById(UUID id) {
		return auctionRepository.findById(id).orElseThrow(() -> new IllegalStateException("Auction not found"));
	}

	public AuctionResponse getAuctionResponse(UUID auctionId) {
		var auction = getAuctionById(auctionId);
		return AuctionMapper.entityToResponse(auction);
	}

	public List<AuctionSummaryResponse> searchAuctions(String title, AuctionStatus status, LocalDateTime startAfter,
			LocalDateTime endBefore) {
		return auctionRepository.search(title, status, startAfter, endBefore).stream()
				.map(AuctionMapper::entityToSummary).toList();
	}

	public void updateAuctionStatus(UUID auctionId, AuctionStatus status) {
		var auction = getAuctionById(auctionId);
		auction.setAuctionStatus(status);
		auctionRepository.save(auction);
	}

	public List<AuctionSummaryResponse> getMyAuctions(UUID appUserId) {
		var auctions = auctionRepository.findMyAuctions(appUserId);
		return auctions.stream().map(AuctionMapper::entityToSummary).toList();
	}

	public List<ItemSummaryResponse> getItemsInAuction(UUID auctionId) {
		var auction = getAuctionById(auctionId);
		var items = auction.getItems();

		if (items == null || items.isEmpty()) {
			return List.of();
		}

		return items.stream().map(ItemMapper::entityToSummary).toList();
	}

	private static void validationAuctionRequest(AuctionRequest request) {
		if (!validateAuctionTime(request)) {
			throw new RuntimeException("Auction timings are invalid");
		}

		if (request.biddingItems() == null || request.biddingItems().isEmpty()) {
			throw new RuntimeException("Auction should have at least one bidding item");
		}
	}

	private static boolean validateAuctionTime(AuctionRequest request) {
		if (request.startTime().isBefore(request.endTime())) {
			return true;
		}

		return !request.startTime().isBefore(LocalDateTime.now()) && !request.endTime().isBefore(LocalDateTime.now());
	}
}
