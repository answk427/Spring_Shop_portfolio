package work.trade.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import work.trade.order.domain.constant.OrderStatusConstant;
import work.trade.order.exception.OrderCannotCancelException;
import work.trade.order.exception.OrderInvalidStatusException;
import work.trade.product.domain.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Builder
    private OrderItem(Product product, Integer quantity, OrderStatus status) {
        this.product = product;
        this.quantity = quantity;
        if (status==null || !status.getCode().equals(OrderStatusConstant.PENDING)) {
            throw new OrderInvalidStatusException("OrderItem은 반드시 PENDING으로 초기화 되어야 합니다.");
        }
        this.status = status;

        this.unitPrice = product.getPrice();

        this.subtotalPrice = this.unitPrice.multiply(new BigDecimal(quantity));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false)
    private OrderStatus status;

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getSubtotalPrice() {
        return this.subtotalPrice;
    }

//*******************************//

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
            case OrderStatusConstant.RETURNED:
                returnOrderItem(nextStatus);
                return;
            default:
                throw new OrderInvalidStatusException("변경하려는 상태가 올바르지 않습니다.");
        }
    }


    //주문 확정 (PENDING → CONFIRM)
    private void confirm(OrderStatus nextStatus) {
        if (!this.status.getCode().equals(OrderStatusConstant.PENDING)) {
            throw new OrderInvalidStatusException("Only pending orders can be confirmed");
        }

        this.status = nextStatus;
    }


    // 배송 시작 (CONFIRMED → SHIPPED)
    private void ship(OrderStatus nextStatus) {
        if (!this.status.getCode().equals(OrderStatusConstant.CONFIRMED)) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }
        this.status = nextStatus;
    }


    // 배송 완료 (SHIPPED → DELIVERED)
    private void deliver(OrderStatus nextStatus) {
        if (!this.status.getCode().equals(OrderStatusConstant.SHIPPED)) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        this.status = nextStatus;
    }


    // 주문 취소 (PENDING 또는 CONFIRMED만 가능)
    private void cancel(OrderStatus nextStatus) {
        if (!canBeCancelled()) {
            throw new OrderCannotCancelException(
                    "현재 상태(" + this.status.getName() + ")에서는 취소할 수 없습니다"
            );
        }

        this.status = nextStatus;
    }

    // 반품 (DELIVERED → RETURNED)
    private void returnOrderItem(OrderStatus nextStatus) {
        if (!this.status.getCode().equals(OrderStatusConstant.DELIVERED)) {
            throw new OrderInvalidStatusException("Only delivered orders can be returned");
        }

        this.status = nextStatus;
    }


    // 주문이 취소 가능한 상태인지 확인
    public boolean canBeCancelled() {
        return this.status.getCode().equals(OrderStatusConstant.PENDING)
                || this.status.getCode().equals(OrderStatusConstant.CONFIRMED);
    }



    // 환불이 필요한 상태인지 확인
    public boolean needsRefund() {
        return this.status.getCode().equals(OrderStatusConstant.DELIVERED);
    }
}
