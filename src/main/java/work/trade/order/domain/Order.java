package work.trade.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.generator.EventType;
import work.trade.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Builder
    private Order(User buyer, List<OrderItem> orderItems) {
        this.buyer = buyer;
        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();

        //총 금액 계산
        this.totalPrice = this.orderItems.stream()
                .map(OrderItem::getSubtotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 양방향 관계 설정
        this.orderItems.forEach(item -> item.setOrder(this));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // 주문 항목들
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    @org.hibernate.annotations.Generated(event = EventType.INSERT)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @org.hibernate.annotations.Generated(event = {EventType.INSERT, EventType.UPDATE})
    private LocalDateTime updatedAt;
}
