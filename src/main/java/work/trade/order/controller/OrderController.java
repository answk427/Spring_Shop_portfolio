package work.trade.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import work.trade.order.dto.request.OrderStatusUpdateRequestDto;
import work.trade.order.dto.response.order.OrderDto;
import work.trade.order.dto.response.order.OrderSummaryDto;
import work.trade.order.service.OrderService;

@RestController
@RequestMapping("/orders")
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

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderSummaryDto>> getUserOrdersByStatus(
            @PathVariable("status") String statusCode,
            Pageable pageable,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Page<OrderSummaryDto> orders = orderService.getUserOrdersByStatus(userId, statusCode, pageable);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> setOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody OrderStatusUpdateRequestDto dto,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderDto orderDto = orderService.executeByStatus(orderId, userId, dto.getStatus());
        return ResponseEntity.ok(orderDto);
    }
}