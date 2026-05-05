package work.trade.order.dto.response.orderItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.order.dto.response.order.OrderStatusDto;
import work.trade.product.dto.response.ProductSummaryDto;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemDto {
    private Long id;
    private ProductSummaryDto product;
    private OrderStatusDto status;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotalPrice;
}
