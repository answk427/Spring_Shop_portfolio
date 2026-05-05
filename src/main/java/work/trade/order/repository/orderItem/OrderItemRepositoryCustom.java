package work.trade.order.repository.orderItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import work.trade.order.dto.response.orderItem.OrderItemSummaryDto;

public interface OrderItemRepositoryCustom {
    Page<OrderItemSummaryDto> findOrderItemsWithPagination(Long buyerId, String statusCode, Pageable pageable);

}
