/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bidder.catalog_service.models.Auction;
import enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionRepository extends JpaRepository<Auction, UUID> {

	@Query("""
			select A
			from Auction A
			where A.auctionStatus in ('LIVE', 'PAUSED')
			and A.endTime <= CURRENT_TIMESTAMP
			""")
	List<Auction> getExpiringAuctions();

	@Query("""
			select a
			from Auction a
			where (cast(:title as string) is null or lower(a.title) like concat('%', lower(cast(:title as string)), '%'))
			and (cast(:status as string) is null or a.auctionStatus = :status)
			and (cast(:startAfter as Instant) is null or a.startTime >= :startAfter)
			and (cast(:endBefore as Instant) is null or a.endTime <= :endBefore)
			""")
	Page<Auction> search(@Param("title") String title, @Param("status") AuctionStatus status,
			@Param("startAfter") Instant startAfter, @Param("endBefore") Instant endBefore, Pageable pageable);

	@Query("""
			select a
			from Auction a
			where a.ownerId = :appUserId
			""")
	Page<Auction> findMyAuctions(@Param("appUserId") UUID appUserId, Pageable pageable);

	@Query("""
			select A
			from Auction A
			where A.auctionStatus in ('UPCOMING')
			and A.startTime <= CURRENT_TIMESTAMP
			""")
	List<Auction> getUpcomingAuctions();

}
