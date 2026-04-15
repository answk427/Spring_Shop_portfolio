package work.trade.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.order.domain.OrderStatus;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, String> {

}
