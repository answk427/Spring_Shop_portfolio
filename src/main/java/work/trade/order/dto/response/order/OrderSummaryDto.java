package work.trade.order.dto.response.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderSummaryDto {
    private Long id;
    private OrderStatusDto status;
    private BigDecimal totalPrice;
    private Integer itemCount; //주문 항목 수
    private LocalDateTime createdAt;
}
