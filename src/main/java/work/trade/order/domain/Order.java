package work.trade.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.generator.EventType;
import work.trade.order.domain.constant.OrderStatusConstant;
import work.trade.order.exception.OrderInvalidStatusException;
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
    private Order(User buyer, List<OrderItem> orderItems, OrderStatus status) {
        this.buyer = buyer;
        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
        this.status = status;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false)
    private OrderStatus status;

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

    /**
     * 주문 확정 (PENDING → CONFIRMED)
     */
    public void advanceOrderStatus(OrderStatus nextStatus) {
        String nextStatusCode = nextStatus.getCode();

        switch (nextStatusCode) {
            case OrderStatusConstant.CONFIRMED:
                confirm(nextStatus);
                return;
            case OrderStatusConstant.SHIPPED:
                ship(nextStatus);
                return;
            case OrderStatusConstant.DELIVERED:
                deliver(nextStatus);
                return;
            case OrderStatusConstant.CANCELLED:
                cancel(nextStatus);
                return;
            default:
                throw new OrderInvalidStatusException("변경하려는 상태가 올바르지 않습니다.");
        }
    }

    /**
     * 주문 확정 (PENDING → CONFIRM)
     */
    private void confirm(OrderStatus nextStatus) {
        if (!this.status.getCode().equals(OrderStatusConstant.PENDING)) {
            throw new OrderInvalidStatusException("Only pending orders can be confirmed");
        }

        this.status = nextStatus;
    }

    /**
     * 배송 시작 (CONFIRMED → SHIPPED)
     */
    private void ship(OrderStatus nextStatus) {
        if (!this.status.getCode().equals(OrderStatusConstant.CONFIRMED)) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }
        this.status = nextStatus;
    }

    /**
     * 배송 완료 (SHIPPED → DELIVERED)
     */
    private void deliver(OrderStatus nextStatus) {
        if (!this.status.getCode().equals(OrderStatusConstant.SHIPPED)) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        this.status = nextStatus;
    }

    /**
     * 주문 취소 (PENDING 또는 CONFIRMED만 가능)
     */
    private void cancel(OrderStatus nextStatus) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel delivered or already cancelled orders");
        }
        this.status = nextStatus;
    }

    /**
     * 주문이 취소 가능한 상태인지 확인
     */
    public boolean canBeCancelled() {
        return this.status.getCode().equals(OrderStatusConstant.PENDING)
                || this.status.getCode().equals(OrderStatusConstant.CONFIRMED);
    }
}
