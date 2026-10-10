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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

	@NotNull private String title;

	@NotNull @Column(name = "owner_id")
	private UUID ownerId;

	@OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Item> items;

	// 15min intervals, 00, 15, 30, 45
	@NotNull private Instant startTime;

	// 15min intervals, 00, 15, 30, 45
	@NotNull private Instant endTime;

	@Enumerated(EnumType.STRING)
	private AuctionStatus auctionStatus;

	@ElementCollection(targetClass = AuctionCategory.class, fetch = FetchType.EAGER)
	@CollectionTable(schema = Constants.Database.SCHEMA, name = "auction_category", joinColumns = @JoinColumn(name = "auction_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private List<AuctionCategory> categories;

	private Instant auctionClosedOn;

	@NotNull @Valid
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "pickup_location", columnDefinition = "jsonb", nullable = false)
	private PickupLocation pickupLocation;
}
