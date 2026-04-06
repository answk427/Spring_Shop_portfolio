package work.trade.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.cart.domain.Cart;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.request.CartUpdateRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.cart.exception.CartNotFoundException;
import work.trade.cart.mapper.CartMapper;
import work.trade.cart.repository.CartRepository;
import work.trade.common.exception.ForbiddenException;
import work.trade.product.domain.Product;
import work.trade.product.exception.ProductNotFoundException;
import work.trade.product.repository.ProductRepository;
import work.trade.user.domain.User;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService{

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper mapper;

    @Override
    public CartDto addToCart(CartAddRequestDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException());

        //이미 담긴 상품이면 수량 추가
        Optional<Cart> existingCart = cartRepository.findByUser_IdAndProduct_Id(user.getId(), product.getId());
        if (existingCart.isPresent()) {
            existingCart.get().addQuantity(dto.getQuantity());
            return mapper.toDto(existingCart.get());
        }

        Cart cart = Cart.builder()
                .user(user)
                .product(product)
                .quantity(dto.getQuantity())
                .build();

        return mapper.toDto(cartRepository.save(cart));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartDto> getMyCart(Long userId) {
        return cartRepository.findByUser_Id(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public CartDto updateQuantity(CartUpdateRequestDto dto, Long cartId, Long userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException());

        if (!cart.getUser().getId().equals(userId)) {
            throw new ForbiddenException();
        }

        cart.updateQuantity(dto.getQuantity());
        return mapper.toDto(cart);
    }

    @Override
    public void deleteCartItem(Long cartId, Long userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException());

        if (!cart.getUser().getId().equals(userId)) {
            throw new ForbiddenException();
        }

        cartRepository.delete(cart);
    }

    @Override
    public void deleteAllCartItems(Long userId) {
        cartRepository.deleteAllByUser_Id(userId);
    }
}
