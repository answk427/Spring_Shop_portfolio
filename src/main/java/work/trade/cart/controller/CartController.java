package work.trade.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.request.CartUpdateRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.cart.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    //장바구니 담기
    @PostMapping
    public ResponseEntity<CartDto> addToCart(
            @RequestBody @Valid CartAddRequestDto dto,
            Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(dto, userId));
    }

    //장바구니 조회
    @GetMapping
    public ResponseEntity<List<CartDto>> getMyCart(Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(cartService.getMyCart(userId));
    }

    //수량 변경
    @PutMapping("/{cartId}")
    public ResponseEntity<CartDto> updateQuantity(
            @PathVariable("cartId") Long cartId,
            @RequestBody @Valid CartUpdateRequestDto dto,
            Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(cartService.updateQuantity(dto, cartId, userId));
    }

    //단건 삭제
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable("cartId") Long cartId,
            Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        cartService.deleteCartItem(cartId, userId);
        return ResponseEntity.noContent().build();
    }

    //전체 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteAllCartItems(Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        cartService.deleteAllCartItems(userId);
        return ResponseEntity.noContent().build();
    }
}
