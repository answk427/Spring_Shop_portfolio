package work.trade.order.dto.response.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long id,
        BigDecimal totalPrice,
        Integer itemCount, //주문 항목 ,
        LocalDateTime createdAt
) {

}
