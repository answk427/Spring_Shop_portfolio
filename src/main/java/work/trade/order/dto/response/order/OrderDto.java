package work.trade.order.dto.response.order;

import work.trade.order.dto.response.orderItem.OrderItemDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        Long buyerId,
        OrderStatusDto status,
        BigDecimal totalPrice,
        List<OrderItemDto>orderItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
