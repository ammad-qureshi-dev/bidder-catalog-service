/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.schedulers;

import java.util.concurrent.TimeUnit;

import com.bidder.catalog_service.repository.AuctionRepository;
import com.bidder.catalog_service.services.AuctionService;
import enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

	private final AuctionService auctionService;
	private final AuctionRepository auctionRepository;

	@Transactional
	@Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
	public void processExpiringAuctions() {
		log.info("scheduler [processExpiringAuctions] -- running");
		final var expiringAuctions = auctionRepository.getExpiringAuctions();
		log.info("scheduler [processExpiringAuctions] -- found {} to be expired auctions", expiringAuctions.size());

		if (expiringAuctions.isEmpty()) {
			return;
		}

		log.info("scheduler [processExpiringAuctions] -- closing auctions");
		expiringAuctions.forEach(auction -> auction.setAuctionStatus(AuctionStatus.CLOSED));
		auctionRepository.saveAll(expiringAuctions);
		log.info("scheduler [processExpiringAuctions] -- set {} auctions to {}", expiringAuctions.size(),
				AuctionStatus.CLOSED);

		log.info("scheduler [processExpiringAuctions] -- processing close auction for {} auctions",
				expiringAuctions.size());
		expiringAuctions.forEach(auctionService::processCloseAuction);
		log.info("scheduler [processExpiringAuctions] -- set {} auctions to {}", expiringAuctions.size(),
				AuctionStatus.CLOSED);
	}

	// ToDo: create scheduler to find UPCOMING events and set to LIVE once they are
	// ready
}
