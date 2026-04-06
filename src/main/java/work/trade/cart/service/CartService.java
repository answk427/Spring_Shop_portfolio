package work.trade.cart.service;

import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.request.CartUpdateRequestDto;
import work.trade.cart.dto.response.CartDto;

import java.util.List;

public interface CartService {
    CartDto addToCart(CartAddRequestDto dto, Long userId);
    List<CartDto> getMyCart(Long userId);
    CartDto updateQuantity(CartUpdateRequestDto dto, Long cartId, Long userId);
    void deleteCartItem(Long cartId, Long userId);
    void deleteAllCartItems(Long userId);
}