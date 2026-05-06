package work.trade.order.repository.orderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.order.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepositoryCustom {
}
