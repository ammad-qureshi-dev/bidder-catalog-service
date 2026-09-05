/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.models;

import java.math.BigDecimal;
import java.util.UUID;

import com.bidder.catalog_service.utils.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = Constants.Database.SCHEMA, name = "item")
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"auction"})
public class Item extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// Entity relationships
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auction")
	private Auction auction;

	@NotNull @Length(max = 128) private String title;

	@Length(max = 512) private String description;

	@DecimalMin("0.0")
	private BigDecimal minimumPrice;

	@DecimalMin("0.0")
	private BigDecimal priceSoldAt;

	private UUID highestBidId;
	private BigDecimal highestBidAmount;

	// For optimistic locking
	@Version
	private Long version;
}
