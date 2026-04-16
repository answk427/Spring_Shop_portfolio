package work.trade.order.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.cart.dto.response.CartItemDto;
import work.trade.cart.exception.CartEmptyException;
import work.trade.cart.service.CartService;
import work.trade.order.domain.Order;
import work.trade.order.domain.OrderItem;
import work.trade.order.domain.OrderStatus;
import work.trade.order.domain.constant.OrderStatusConstant;
import work.trade.order.dto.response.order.OrderDto;
import work.trade.order.dto.response.order.OrderSummaryDto;
import work.trade.order.exception.OrderCannotCancelException;
import work.trade.order.exception.OrderInvalidStatusException;
import work.trade.order.exception.OrderNotFoundException;
import work.trade.order.exception.OrderStatusNotFoundException;
import work.trade.order.mapper.OrderMapper;
import work.trade.order.repository.OrderRepository;
import work.trade.order.repository.OrderStatusRepository;
import work.trade.product.domain.Product;
import work.trade.product.exception.ProductNotFoundException;
import work.trade.product.repository.ProductRepository;
import work.trade.user.domain.User;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;

    private final CartService cartService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderStatusRepository orderStatusRepository;

    private final OrderMapper orderMapper;

    private final EntityManager em;
    /**
     * 장바구니에서 주문 생성
     * 1. 사용자 및 장바구니 검증
     * 2. 각 장바구니 항목에 대해 재고 확인 및 감소
     * 3. OrderItem 생성
     * 4. Order 생성 및 저장
     * 5. 장바구니 비우기
     */
    @Transactional()
    public OrderDto createOrderFromCart(Long userId) {
        log.info("주문 생성 시작 - userId: {}", userId);

        //1. 사용자 검증
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        //2. 장바구니에 있는 상품들의 ID를 가져옴
        List<CartItemDto> cartItemIdsForOrder = cartService.getCartItemIdsForOrder(userId);
        if (cartItemIdsForOrder.isEmpty()) {
            throw new CartEmptyException();
        }

        //3. 주문 항목 생성 및 재고 관리
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDto cart : cartItemIdsForOrder) {
            log.info("Thread: {} product 락 획득 시도", Thread.currentThread().getName());
            Product product = productRepository.findByIdWithLock(cart.productId())
                    .orElseThrow(() -> new ProductNotFoundException());
            log.info("Thread: {} product 락 획득 성공", Thread.currentThread().getName());

            //재고 감소
            log.info("재고 감소 전 product id:{}, product stock:{}", product.getId(), product.getStock());
            product.decreaseStock(cart.quantity());
            log.info("재고 감소 후 product stock : {}", product.getStock());

            //OrderItem 생성
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cart.quantity())
                    .build();
            orderItems.add(orderItem);

            log.info("주문 항목 생성 - productId: {}, quantity: {}", product.getId(), cart.quantity());
        }

        //4. Order 생성 및 저장
        OrderStatus pendingStatus = orderStatusRepository.findById(OrderStatusConstant.PENDING)
                .orElseThrow(() -> new OrderStatusNotFoundException());

        Order order = Order.builder()
                .buyer(buyer)
                .orderItems(orderItems)
                .status(pendingStatus)
                .build();

        Order savedOrder = orderRepository.save(order);

        //장바구니 비우기
        cartService.deleteAllCartItems(userId);
        log.info("주문 생성 완료 - orderId: {}, totalPrice: {}", savedOrder.getId(), savedOrder.getTotalPrice());

        return orderMapper.toOrderDto(savedOrder);
    }

    //주문 상세 조회
    @Transactional(readOnly = true)
    public OrderDto getOrder(Long orderId, Long userId) {
        Order order = getOrderByIdAndUserId(orderId, userId);
        return orderMapper.toOrderDto(order);
    }

    //사용자의 모든 주문 조회
    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByBuyer_IdOrderByCreatedAtDesc(userId, pageable)
                .map(orderMapper::toOrderSummaryDto);
    }

    //특정 상태의 주문 조회
    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByBuyer_IdAndStatus(userId, status, pageable)
                .map(orderMapper::toOrderSummaryDto);
    }

    public OrderDto executeByStatus(Long orderId, Long userId, String targetStatus) {
        switch (targetStatus) {
            case OrderStatusConstant.CONFIRMED:
                return confirmOrder(orderId, userId);      // PENDING → CONFIRMED
            case OrderStatusConstant.SHIPPED:
                return shipOrder(orderId, userId);         // CONFIRMED → SHIPPED
            case OrderStatusConstant.DELIVERED:
                return deliverOrder(orderId, userId);      // SHIPPED → DELIVERED
            case OrderStatusConstant.CANCELLED:
                return cancelOrder(orderId, userId);       // PENDING/CONFIRMED → CANCELLED
            default:
                throw new OrderInvalidStatusException("변경하려는 상태가 올바르지 않습니다.");
        }
    }

    //주문 확정(PENDING -> CONFIRMED)
    private OrderDto confirmOrder(Long orderId, Long userId) {
        Order order = getOrderByIdAndUserId(orderId, userId);
        OrderStatus nextStatus = orderStatusRepository.findById(OrderStatusConstant.CONFIRMED)
                .orElseThrow(() -> new OrderStatusNotFoundException());

        advanceOrderStatus(order, nextStatus);
        log.info("주문 확정 - orderId: {}", order.getId());

        return orderMapper.toOrderDto(order);
    }

    //배송 시작(CONFIRMED -> SHIPPED)
    private OrderDto shipOrder(Long orderId, Long userId) {
        Order order = getOrderByIdAndUserId(orderId, userId);
        OrderStatus nextStatus = orderStatusRepository.findById(OrderStatusConstant.SHIPPED)
                .orElseThrow(() -> new OrderStatusNotFoundException());

        advanceOrderStatus(order, nextStatus);
        log.info("배송 시작 - orderId: {}", order.getId());

        return orderMapper.toOrderDto(order);
    }

    //배송 완료(SHIPPED -> DELIVERED)
    private OrderDto deliverOrder(Long orderId, Long userId) {
        Order order = getOrderByIdAndUserId(orderId, userId);
        OrderStatus nextStatus = orderStatusRepository.findById(OrderStatusConstant.DELIVERED)
                .orElseThrow(() -> new OrderStatusNotFoundException());

        advanceOrderStatus(order, nextStatus);
        log.info("배송 완료 - orderId: {}", order.getId());

        return orderMapper.toOrderDto(order);
    }

    //주문 취소
    private OrderDto cancelOrder(Long orderId, Long userId) {
        Order order = getOrderByIdAndUserId(orderId, userId);

        if (!order.canBeCancelled()) {
            throw new OrderCannotCancelException(
                    "현재 상태(" + order.getStatus().getName() + ")에서는 취소할 수 없습니다"
            );
        }

        //재고 복구
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            log.debug("재고 복구 - productId: {}, quantity: {}", product.getId(), item.getQuantity());
        }

        OrderStatus nextStatus = orderStatusRepository.findById(OrderStatusConstant.CANCELLED)
                .orElseThrow(() -> new OrderStatusNotFoundException());

        advanceOrderStatus(order, nextStatus);

        log.info("주문 취소 - orderId: {}", orderId);

        return orderMapper.toOrderDto(order);
    }
    /**
     * 사용자의 주문 조회 (권한 검증)
     */
    private Order getOrderByIdAndUserId(Long orderId, Long userId) {
        return orderRepository.findByIdAndBuyer_Id(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException());
    }

    private void advanceOrderStatus(Order order, OrderStatus nextStatus) {
        try {
            order.advanceOrderStatus(nextStatus);
        } catch (Exception e) {
            throw new OrderInvalidStatusException(e.getMessage());
        }
    }
}
