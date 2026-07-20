package models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
//	@Length(max = 128)
	private String title;

//	@ManyToOne
//	@JoinColumn(name = "owner_id")
//	private AppUser owner;

	@OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Item> items;

	@NotNull private LocalDateTime startTime;

	@NotNull private LocalDateTime endTime;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private AuctionStatus auctionStatus = AuctionStatus.OPEN;

	@ElementCollection(targetClass = AuctionCategory.class, fetch = FetchType.EAGER)
	@CollectionTable(schema = Constants.Database.SCHEMA, name = "auction_category", joinColumns = @JoinColumn(name = "auction_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private List<AuctionCategory> categories;

	// ToDo: add location
}
