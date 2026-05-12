package work.trade.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import work.trade.order.dto.response.order.OrderDto;
import work.trade.order.dto.response.order.OrderSummaryDto;
import work.trade.order.dto.response.orderItem.OrderItemDto;
import work.trade.order.dto.response.orderItem.OrderItemSummaryDto;
import work.trade.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderDto orderDto = orderService.createOrderFromCart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(
            @PathVariable("orderId") Long orderId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderDto orderDto = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummaryDto>> getMyOrders(
            Pageable pageable,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Page<OrderSummaryDto> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderItemSummaryDto>> getUserOrdersByStatus(
            @PathVariable("status") String statusCode,
            Pageable pageable,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Page<OrderItemSummaryDto> orders = orderService.getOrderItemsByStatus(userId, statusCode, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/seller/status/{status}")
    public ResponseEntity<Page<OrderItemSummaryDto>> getSellerOrdersByStatus(
            @PathVariable("status") String statusCode,
            Pageable pageable,
            Authentication authentication) {
        Long sellerId = Long.parseLong(authentication.getName());
        Page<OrderItemSummaryDto> orders = orderService.getSellerOrderItemsByStatus(sellerId, statusCode, pageable);
        return ResponseEntity.ok(orders);
    }

//Order 전체단위 Status 변경*******************************//

    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<OrderDto> confirmOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderDto orderDto = orderService.confirmOrder(orderId, userId);
        return ResponseEntity.ok(orderDto);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderDto orderDto = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(orderDto);
    }

//Order Item 단위(특정 상품) Status 변경*******************************//

    @PatchMapping("/items/{orderItemId}/ship")
    public ResponseEntity<OrderItemDto> shipOrderItem(
            @PathVariable Long orderItemId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderItemDto orderItemDto = orderService.shipOrderItem(orderItemId, userId);
        return ResponseEntity.ok(orderItemDto);
    }

    @PatchMapping("/items/{orderItemId}/deliver")
    public ResponseEntity<OrderItemDto> deliverOrderItem(
            @PathVariable Long orderItemId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderItemDto orderItemDto = orderService.deliverOrderItem(orderItemId, userId);
        return ResponseEntity.ok(orderItemDto);
    }

    @PatchMapping("/items/{orderItemId}/cancel")
    public ResponseEntity<OrderItemDto> cancelOrderItem(
            @PathVariable Long orderItemId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderItemDto orderItemDto = orderService.cancelOrderItem(orderItemId, userId);
        return ResponseEntity.ok(orderItemDto);
    }

    @PatchMapping("/items/{orderItemId}/return")
    public ResponseEntity<OrderItemDto> returnOrderItem(
            @PathVariable Long orderItemId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderItemDto orderItemDto = orderService.returnOrderItem(orderItemId, userId);
        return ResponseEntity.ok(orderItemDto);
    }
}