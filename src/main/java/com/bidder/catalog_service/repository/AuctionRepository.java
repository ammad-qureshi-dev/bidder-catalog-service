/* (C) 2026
bidder.app */
package com.bidder.catalog_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bidder.catalog_service.models.Auction;
import enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionRepository extends JpaRepository<Auction, UUID> {

	@Query("""
			select A
			from Auction A
			where A.status in ('LIVE', 'PAUSED')
			and A.endTime <= CURRENT_TIMESTAMP
			""")
	List<Auction> getExpiringAuctions();

	@Query("""
			select a
			from Auction a
			where (cast(:title as string) is null or lower(a.title) like concat('%', lower(cast(:title as string)), '%'))
			and (cast(:status as string) is null or a.auctionStatus = :status)
			and (cast(:startAfter as LocalDateTime) is null or a.startTime >= :startAfter)
			and (cast(:endBefore as LocalDateTime) is null or a.endTime <= :endBefore)
			""")
	List<Auction> search(@Param("title") String title, @Param("status") AuctionStatus status,
			@Param("startAfter") LocalDateTime startAfter, @Param("endBefore") LocalDateTime endBefore);

	@Query("""
			select a
			from Auction a
			where a.ownerId = :appUserId
			""")
	List<Auction> findMyAuctions(@Param("appUserId") UUID appUserId);

}
