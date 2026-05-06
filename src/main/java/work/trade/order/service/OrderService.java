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
import work.trade.order.dto.response.orderItem.OrderItemDto;
import work.trade.order.dto.response.orderItem.OrderItemSummaryDto;
import work.trade.order.exception.OrderCannotCancelException;
import work.trade.order.exception.OrderItemNotFoundException;
import work.trade.order.exception.OrderNotFoundException;
import work.trade.order.exception.OrderStatusNotFoundException;
import work.trade.order.mapper.OrderMapper;
import work.trade.order.repository.OrderStatusRepository;
import work.trade.order.repository.order.OrderRepository;
import work.trade.order.repository.orderItem.OrderItemRepository;
import work.trade.product.domain.Product;
import work.trade.product.exception.ProductNotFoundException;
import work.trade.product.repository.ProductRepository;
import work.trade.user.domain.User;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.exception.UserUnAuthorizedException;
import work.trade.user.repository.UserRepository;
import work.trade.wallet.service.WalletService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final CartService cartService;
    private final WalletService walletService;

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final OrderMapper orderMapper;

    private final EntityManager em;
    private final OrderItemRepository orderItemRepository;

//*******************************//

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
        List<Long> productIds = cartItemIdsForOrder.stream()
                .map(CartItemDto::productId)
                .sorted()
                .toList();

        log.debug("createOrder / Thread: {} product 락 획득 시도", Thread.currentThread().getName());
        List<Product> products = productRepository.findAllByIdWithLock(productIds);
        log.debug("createOrder / Thread: {} product 락 획득 성공", Thread.currentThread().getName());

        OrderStatus pendingStatus = orderStatusRepository.findById(OrderStatusConstant.PENDING).
                orElseThrow(() -> new OrderNotFoundException());

        List<OrderItem> orderItems = cartItemIdsForOrder.stream().map(cartItemDto -> {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(cartItemDto.productId())).
                    findFirst().get();

            product.decreaseStock(cartItemDto.quantity());
            return OrderItem.builder()
                    .product(product)
                    .quantity(cartItemDto.quantity())
                    .status(pendingStatus)
                    .build();
        }).toList();

        //4. Order 생성 및 저장
        Order order = Order.builder()
                .buyer(buyer)
                .orderItems(orderItems)
                .build();

        Order savedOrder = orderRepository.save(order);

        //장바구니 비우기
        cartService.deleteAllCartItemsInBatch(userId);
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
        return orderRepository.findOrdersWithPagination(userId, pageable);
    }

    //특정 상태의 모든 주문 조회
    @Transactional(readOnly = true)
    public Page<OrderItemSummaryDto> getOrderItemsByStatus(Long userId, String statusCode, Pageable pageable) {
        OrderStatus status = orderStatusRepository.findById(statusCode)
                .orElseThrow(() -> new OrderStatusNotFoundException());

        return orderItemRepository.findOrderItemsWithPagination(userId, statusCode, pageable);
    }


//Order Status 변경 함수*******************************//

    //주문 확정(PENDING -> CONFIRMED)
    //CONFIRM은 ORDER 단위로 결제
    public OrderDto confirmOrder(Long orderId, Long userId) {
        Order order = getOrderByIdAndUserId(orderId, userId);

        //구매자 Wallet에서 잔고 차감
        walletService.deduct(userId, order.getTotalPrice());

        OrderStatus confirmState = orderStatusRepository.findById(OrderStatusConstant.CONFIRMED)
                .orElseThrow(() -> new OrderStatusNotFoundException());
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItem.advanceOrderStatus(confirmState);
        }

        log.info("주문 확정 - orderId: {}", order.getId());

        return orderMapper.toOrderDto(order);
    }

    //Order 단위로 Cancel
    public OrderDto cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndBuyer_IdItemsFetchJoin(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException());

        //재고 복구(LOCK 필요)
        List<Long> productIds = order.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .distinct()
                .sorted()
                .toList();

        log.debug("cancelOrder / Thread: {} product 락 획득 시도", Thread.currentThread().getName());
        List<Product> products = productRepository.findAllByIdWithLock(productIds);
        log.debug("cancelOrder / Thread: {} product 락 획득 시도", Thread.currentThread().getName());

        //product Id와 매칭되는 맵 생성
        Map<Long, Product> productMap = products.stream().
                collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderItem item : order.getOrderItems()) {
            Product product = productMap.get(item.getProduct().getId());

            //상태변경, 재고처리, 환불
            try {
                cancelOrderItemInternal(item, product);
            } catch (OrderCannotCancelException e) {
                throw new OrderCannotCancelException(
                        String.format("주문상품 ID:%d가 취소할 수 없는 상태입니다. \n error: %s",
                                item.getId(), e)
                );
            }
        }

        log.info("주문 취소 - orderId: {}", orderId);

        return orderMapper.toOrderDto(order);
    }

//Order Status 변경 함수*******************************//

    //배송 시작(CONFIRMED -> SHIPPED)
    public OrderItemDto shipOrderItem(Long orderItemId, Long userId) {
        OrderItem orderItem = advanceOrderItemStatusInternal
                (orderItemId, userId, OrderStatusConstant.SHIPPED);

        log.info("배송 시작 - orderId: {}", orderItem.getId());

        return orderMapper.toOrderItemDto(orderItem);
    }

    //배송 완료(SHIPPED -> DELIVERED)
    public OrderItemDto deliverOrderItem(Long orderItemId, Long userId) {
        OrderItem orderItem = advanceOrderItemStatusInternal
                (orderItemId, userId, OrderStatusConstant.DELIVERED);

        //배송이 완료되면 판매자에게 입금됨.
        if (orderItem.needsRefund()) {
            Long sellerId = orderItem.getProduct().getSeller().getId();
            walletService.deposit(sellerId, orderItem.getSubtotalPrice());
        }

        log.info("배송 완료 - orderId: {}", orderItem.getId());
        return orderMapper.toOrderItemDto(orderItem);
    }

    public OrderItemDto cancelOrderItem(Long orderItemId, Long userId) {
        OrderItem orderItem = advanceOrderItemStatusInternal
                (orderItemId, userId, OrderStatusConstant.CANCELLED);

        //재고 복구(LOCK 필요)
        Long productId = orderItem.getProduct().getId();

        log.debug("cancelOrderItem / Thread: {} product 락 획득 시도", Thread.currentThread().getName());
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException());
        log.debug("cancelOrderItem / Thread: {} product 락 획득 시도", Thread.currentThread().getName());

        cancelOrderItemInternal(orderItem, product);

        log.info("주문 취소 - orderItemId: {}", orderItemId);

        return orderMapper.toOrderItemDto(orderItem);
    }

    public OrderItemDto returnOrderItem(Long orderItemId, Long userId) {
        OrderItem orderItem = getOrderItem(orderItemId, userId);

        //재고복구
        Long productId = orderItem.getProduct().getId();
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException());

        product.increaseStock(orderItem.getQuantity());

        if (orderItem.needsRefund()) {
            //판매자의 Wallet에서 환불된 만큼 차감
            walletService.deductByRefund(product.getSeller().getId(), orderItem.getSubtotalPrice());
            //구매자의 Wallet에 환불된 만큼 복구
            walletService.depositByRefund(userId, orderItem.getSubtotalPrice());
        }

        advanceOrderItemStatusInternal(orderItemId, userId, OrderStatusConstant.RETURNED);

        log.info("반품 완료 - orderId: {}", orderItemId);

        return orderMapper.toOrderItemDto(orderItem);
    }

//*******************************//

    // 사용자의 주문 조회 (권한 검증)
    private Order getOrderByIdAndUserId(Long orderId, Long userId) {
        return orderRepository.findByIdAndBuyer_IdFetchJoin(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException());
    }

    private OrderItem getOrderItem(Long orderItemId, Long userId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).
                orElseThrow(() -> new OrderItemNotFoundException());

        if (!userId.equals(orderItem.getOrder().getBuyer().getId())) {
            throw new UserUnAuthorizedException();
        }
        return orderItem;
    }

    private OrderItem advanceOrderItemStatusInternal(Long orderItemId, Long userId, String nextStatusCode) {
        OrderItem orderItem = getOrderItem(orderItemId, userId);
        OrderStatus nextStatus = orderStatusRepository.findById(nextStatusCode)
                .orElseThrow(() -> new OrderStatusNotFoundException());

        orderItem.advanceOrderStatus(nextStatus);

        return orderItem;
    }

    private void cancelOrderItemInternal(OrderItem orderItem, Product product) {
        // 취소 상태로 변경
        OrderStatus cancelStatus = orderStatusRepository.findById(OrderStatusConstant.CANCELLED)
                .orElseThrow(() -> new OrderStatusNotFoundException());
        orderItem.advanceOrderStatus(cancelStatus);

        // 재고 복구
        product.increaseStock(orderItem.getQuantity());
        log.debug("재고 복구 - productId: {}, quantity: {}",
                product.getId(), orderItem.getQuantity());
    }
}
