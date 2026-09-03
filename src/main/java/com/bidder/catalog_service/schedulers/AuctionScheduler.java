/* (C) 2026
bidder.app */
package com.bidder.catalog_service.schedulers;

import com.bidder.catalog_service.repository.AuctionRepository;
import com.bidder.catalog_service.services.AuctionService;
import lombok.RequiredArgsConstructor;
import enums.AuctionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

	private final AuctionService auctionService;
	private final AuctionRepository auctionRepository;

	@Transactional
	@Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
	public void processExpiringAuctions() {
		log.debug("scheduler [processExpiringAuctions] -- running");
		final var expiringAuctions = auctionRepository.getExpiringAuctions();
		log.debug("scheduler [processExpiringAuctions] -- found {} to be expired auctions", expiringAuctions.size());

		if (expiringAuctions.isEmpty()) {
			return;
		}

		log.debug("scheduler [processExpiringAuctions] -- closing auctions");
		expiringAuctions.forEach(auction -> auction.setAuctionStatus(AuctionStatus.CLOSED));
		auctionRepository.saveAll(expiringAuctions);
		log.debug("scheduler [processExpiringAuctions] -- set {} auctions to {}", expiringAuctions.size(), AuctionStatus.CLOSED);

		log.debug("scheduler [processExpiringAuctions] -- processing close auction for {} auctions", expiringAuctions.size());
		expiringAuctions.forEach(auctionService::processCloseAuction);
		log.debug("scheduler [processExpiringAuctions] -- set {} auctions to {}", expiringAuctions.size(), AuctionStatus.CLOSED);
	}
}
