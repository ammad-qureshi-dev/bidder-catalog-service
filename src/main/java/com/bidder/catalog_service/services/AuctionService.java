/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.bidder.catalog_service.http_client.BiddingServiceClient;
import com.bidder.catalog_service.http_client.IdentityAndAuthServiceClient;
import com.bidder.catalog_service.mappers.AuctionMapper;
import com.bidder.catalog_service.mappers.ItemMapper;
import com.bidder.catalog_service.models.Auction;
import com.bidder.catalog_service.models.Item;
import com.bidder.catalog_service.repository.AuctionRepository;
import config.EventTopics;
import dtos.request.AuctionRequest;
import dtos.response.AuctionResponse;
import dtos.response.PageResponse;
import dtos.response.PreferredContactMethod;
import dtos.response.summary.AuctionSummaryResponse;
import dtos.response.summary.ItemSummaryResponse;
import enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import models.TemplateName;
import models.dtos.request.SendNotificationRequest;
import models.dtos.response.summary.BidSummaryResponse;
import models.entities.BidStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

	@Value("${client-url}")
	private String clientUrl;

	private final AuctionRepository auctionRepository;
	private final ItemService itemService;
	private final BiddingServiceClient biddingService;
	private final IdentityAndAuthServiceClient identityAndAuthService;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	public UUID createAuction(AuctionRequest request, UUID appUserId) {
		var isFutureDated = isFutureDated(request.startTime());

		validationAuctionRequest(request);

		final var auction = AuctionMapper.requestToEntity(request);
		auction.setOwnerId(appUserId);

		auction.setAuctionStatus(isFutureDated ? AuctionStatus.UPCOMING : AuctionStatus.LIVE);

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

	public PageResponse<AuctionSummaryResponse> searchAuctions(String title, AuctionStatus status, Instant startAfter,
			Instant endBefore, Pageable pageable) {
		var auctions = auctionRepository.search(title, status, startAfter, endBefore, pageable)
				.map(AuctionMapper::entityToSummary);
		return PageResponse.from(auctions);
	}

	private static boolean isAuctionOpen(final Auction auction) {
		final var currStatus = auction.getAuctionStatus();
		return !(AuctionStatus.CLOSED.equals(currStatus) || Instant.now().isAfter(auction.getEndTime()));
	}

	@Transactional
	public void updateAuctionStatus(UUID auctionId, AuctionStatus status) {
		final var auction = getAuctionById(auctionId);
		final var currStatus = auction.getAuctionStatus();

		if (currStatus.equals(status)) {
			return;
		}

		if (status.equals(AuctionStatus.LIVE) || status.equals(AuctionStatus.PAUSED)) {
			if (!isAuctionOpen(auction)) {
				throw new IllegalStateException("Auction is " + AuctionStatus.CLOSED + ", cannot perform update");
			}

			auction.setAuctionStatus(status);
			auctionRepository.save(auction);
		} else if (status.equals(AuctionStatus.CLOSED)) {
			if (!isAuctionOpen(auction)) {
				// no need to throw exception
				return;
			}

			auction.setAuctionStatus(status);
			auction.setAuctionClosedOn(Instant.now());
			auctionRepository.save(auction);
			processCloseAuction(auction);
		}

		notifyAuctionStatusToOwner(auction);
	}

	public PageResponse<AuctionSummaryResponse> getMyAuctions(UUID appUserId, Pageable pageable) {
		var auctions = auctionRepository.findMyAuctions(appUserId, pageable).map(AuctionMapper::entityToSummary);
		return PageResponse.from(auctions);
	}

	public List<ItemSummaryResponse> getItemsInAuction(UUID auctionId) {
		var auction = getAuctionById(auctionId);
		var items = auction.getItems();

		if (items == null || items.isEmpty()) {
			return List.of();
		}

		return items.stream().map(ItemMapper::entityToSummary).toList();
	}

	public void processCloseAuction(Auction auction) {
		// items with no bids have a null highestBidId
		var itemsWithBids = auction.getItems().stream().filter(i -> i.getHighestBidId() != null).toList();
		if (itemsWithBids.isEmpty()) {
			notifyAuctionStatusToOwner(auction);
			return;
		}

		// call /api/v1/bid/search?bidIds=... to get all the bidderIds
		var bids = biddingService.searchByBidIds(itemsWithBids.stream().map(Item::getHighestBidId).toList());

		// id <-> bid
		var bidsById = bids.stream().collect(Collectors.toMap(BidSummaryResponse::id, b -> b, (a, b) -> a));

		biddingService.bulkUpdateBidsStatuses(bidsById.keySet().stream().toList(), BidStatus.WINNER);

		var bidderIds = bids.stream().map(BidSummaryResponse::bidderId).distinct().toList();

		// call auth per bidderId to get contact info
		var contactMethods = identityAndAuthService.internalGetPreferredContactMethods(bidderIds);
		var contactMethodsByAppUserId = contactMethods.stream()
				.collect(Collectors.toMap(PreferredContactMethod::appUserId, c -> c, (a, b) -> a));

		// per bidder Id send message
		for (var item : itemsWithBids) {
			var bid = bidsById.get(item.getHighestBidId());
			if (bid == null) {
				log.warn("Bid {} for item {} not found in bidding-service", item.getHighestBidId(), item.getId());
				continue;
			}

			var contactInfo = contactMethodsByAppUserId.get(bid.bidderId());
			if (contactInfo == null) {
				log.warn("No contact method found for bidder {}", bid.bidderId());
				continue;
			}

			var auctionUrl = clientUrl + "/auctions/" + item.getAuction().getId();

			kafkaTemplate.send(EventTopics.NOTIFICATION.getTopic(),
					new SendNotificationRequest(contactInfo.appUserId(), TemplateName.BID_REQUEST_ACCEPTED,
							Map.of(contactInfo.type(), contactInfo.value()), Map.of("fullName", "user", "itemName",
									item.getTitle(), "bidAmount", bid.amount(), "auctionUrl", auctionUrl)));

		}

	}

	public void notifyAuctionStatusToOwner(Auction auction) {
		var owner = identityAndAuthService.getAppUserById(auction.getOwnerId());

		if (owner.isEmpty()) {
			throw new NoSuchElementException("Owner not found for this auction");
		}

		var ownerInfo = owner.get();

		var recipientConfig = Map.of(ownerInfo.contact().type(), ownerInfo.contact().value());

		var templateData = new HashMap<String, Object>();
		TemplateName templateName = null;

		var auctionUrl = clientUrl + "/auctions/" + auction.getId();

		var currStatus = auction.getAuctionStatus();
		switch (currStatus) {
			case AuctionStatus.CLOSED -> {
				templateName = TemplateName.AUCTION_CLOSED;
				templateData = new HashMap<>(Map.of("fullName", ownerInfo.fullName(), "auctionUrl", auctionUrl, "items",
						auction.getItems()));
			}

			case AuctionStatus.LIVE -> {
				templateName = TemplateName.AUCTION_LIVE;
				templateData = new HashMap<>(Map.of("fullName", ownerInfo.fullName(), "auctionUrl", auctionUrl, "title",
						auction.getTitle()));
			}

			case AuctionStatus.PAUSED -> {
				templateName = TemplateName.AUCTION_PAUSED;
				templateData = new HashMap<>(Map.of("fullName", ownerInfo.fullName(), "auctionUrl", auctionUrl, "title",
						auction.getTitle()));
			}
		}

		var notifyRequest = new SendNotificationRequest(auction.getOwnerId(), templateName, recipientConfig,
				templateData);

		kafkaTemplate.send(EventTopics.NOTIFICATION.getTopic(), notifyRequest);
	}

	private static void validationAuctionRequest(AuctionRequest request) {
		if (!validateAuctionRequestTime(request)) {
			throw new RuntimeException("Auction timings are invalid");
		}

		if (request.biddingItems() == null || request.biddingItems().isEmpty()) {
			throw new RuntimeException("Auction should have at least one bidding item");
		}
	}

	private static boolean validateAuctionRequestTime(AuctionRequest request) {
		if (!request.startTime().isBefore(request.endTime())) {
			return false;
		}

		if (isFutureDated(request.startTime())) {
			return isOnQuarterHour(request.endTime());
		}

		return isOnQuarterHour(request.startTime()) && isOnQuarterHour(request.endTime());
	}

	private static boolean isOnQuarterHour(Instant dateTime) {
		return dateTime.equals(dateTime.truncatedTo(ChronoUnit.MINUTES)) && dateTime.getEpochSecond() % 900 == 0;
	}

	private static boolean isFutureDated(Instant auctionStartTime) {
		return auctionStartTime.isAfter(Instant.now());
	}
}
