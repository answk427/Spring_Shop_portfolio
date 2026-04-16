package work.trade.order.dto.response.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.order.domain.OrderStatus;
import work.trade.order.dto.response.orderItem.OrderItemDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private Long buyerId;
    private OrderStatusDto status;
    private BigDecimal totalPrice;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
