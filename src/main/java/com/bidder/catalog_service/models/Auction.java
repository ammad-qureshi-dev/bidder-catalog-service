/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bidder.catalog_service.utils.Constants;
import enums.AuctionCategory;
import enums.AuctionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = Constants.Database.SCHEMA, name = "auction")
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"items"})
public class Auction extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotNull
	// @Length(max = 128)
	private String title;

	// References AppUser.id owned by identity-auth-service; no cross-service JPA
	// relation
	@NotNull @Column(name = "owner_id")
	private UUID ownerId;

	@OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Item> items;

	// 15min intervals, 00, 15, 30, 45
	@NotNull private Instant startTime;

	// 15min intervals, 00, 15, 30, 45
	@NotNull private Instant endTime;

	@Builder.Default
	@Enumerated(EnumType.STRING)

	// ToDo: set to LIVE if auction made right now, else set to UPCOMING if
	// future-dated
	private AuctionStatus auctionStatus = AuctionStatus.LIVE;

	@ElementCollection(targetClass = AuctionCategory.class, fetch = FetchType.EAGER)
	@CollectionTable(schema = Constants.Database.SCHEMA, name = "auction_category", joinColumns = @JoinColumn(name = "auction_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private List<AuctionCategory> categories;

	// ToDo: add location

	// MAJOR TODO --> RESET ALL import java.time.LocalDateTime; TO INSTANT. ADD
	// import java.time.LocalDateTime; to INSTANT conversion on inputs

}
