/* (C) 2026
bidder.app */
package com.bidder.catalog_service.schedulers;

import java.util.HashSet;

import com.bidder.catalog_service.models.CommsStatus;
import com.bidder.catalog_service.repository.AuctionRepository;
import com.bidder.catalog_service.services.CommunicationService;
import lombok.RequiredArgsConstructor;
import enums.AuctionStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuctionScheduler {

	private final CommunicationService communicationService;
	private final AuctionRepository auctionRepository;

	@Transactional
	@Scheduled(fixedRate = 60000)
	public void closeAuctions() {
		final var closedAuctions = auctionRepository.findOpenAndPausedAuctions();

		if (closedAuctions.isEmpty()) {
			return;
		}

		closedAuctions.forEach(auction -> auction.setAuctionStatus(AuctionStatus.CLOSED));
		auctionRepository.saveAll(closedAuctions);

		// ToDo: send comms to auctioneers about auctions closed
		final var unsentComms = new HashSet<>();
		closedAuctions.forEach(auction -> {

			var commsStatus = communicationService.sendCommunication(
					"Your auction " + auction.getTitle() + " - " + auction.getId() + " has been completed.");
			if (!CommsStatus.SENT.equals(commsStatus)) {
				// ToDo: add to retry
				unsentComms.add(auction.getId());
			}
		});
	}
}
