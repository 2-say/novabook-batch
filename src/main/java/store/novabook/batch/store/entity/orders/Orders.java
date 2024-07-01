package store.novabook.batch.store.entity.orders;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.novabook.batch.store.entity.member.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Orders {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "delivery_fee_id")
	private DeliveryFee deliveryFee;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "wrapping_paper_id")
	private WrappingPaper wrappingPaper;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "orders_status_id")
	private OrdersStatus ordersStatus;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	@NotNull
	private LocalDateTime ordersDate;

	@NotNull
	private Long totalAmount;

	@NotNull
	private LocalDateTime deliveryDate;

	@NotNull
	private long bookPurchaseAmount;

	@NotNull
	private String deliveryAddress;

	@NotNull
	private String receiverName;

	@NotNull
	private String receiverNumber;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}
