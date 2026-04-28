package work.trade.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import work.trade.order.domain.Order;
import work.trade.order.domain.OrderStatus;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    //사용자의 특정 주문 조회
    Optional<Order> findByIdAndBuyer_Id(Long orderId, Long buyerId);

    //모든 주문 조회
    Page<Order> findByBuyer_IdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    //사용자의 주문 개수
    long countByBuyer_Id(Long buyerId);

    //특정 상태의 주문 조회
    @Query("SELECT o FROM Order o WHERE o.buyer.id = :buyerId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByBuyer_IdAndStatus(
            @Param("buyerId") Long buyerId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    //===============JOIN FETCH FUNC==================//
    @Query("select o from Order o " +
            "join fetch o.status " +
            "join fetch o.orderItems oi " +
            "join fetch oi.product p " +
            "join fetch p.category " +
            "join fetch p.seller " +
            "where o.id = :orderId and o.buyer.id = :userId")
    Optional<Order> findByIdAndBuyer_IdFetchJoin(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
