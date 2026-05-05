package work.trade.order.dto.response.orderItem;

import work.trade.order.dto.response.order.OrderStatusDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemSummaryDto(
        Long id,
        String productName,
        OrderStatusDto status,
        Integer quantity,
        BigDecimal subtotalPrice,
        LocalDateTime createdAt
) {

}
