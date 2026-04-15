package work.trade.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import work.trade.product.domain.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Builder
    private OrderItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;

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


    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getSubtotalPrice() {
        return this.subtotalPrice;
    }
}
