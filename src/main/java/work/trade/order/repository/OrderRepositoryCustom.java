package work.trade.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import work.trade.order.dto.response.order.OrderSummaryDto;

public interface OrderRepositoryCustom {
    Page<OrderSummaryDto> findOrdersWithPagination(Long buyerId, String statusCode, Pageable pageable);
}
