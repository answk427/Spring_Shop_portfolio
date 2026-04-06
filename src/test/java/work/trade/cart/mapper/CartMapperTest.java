package work.trade.cart.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;
import work.trade.auth.role.Role;
import work.trade.cart.domain.Cart;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.service.ProductService;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.service.UserService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartMapperTest {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;


    @Test
    void toEntity() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto();
        cartAddRequestDto.setQuantity(100);
        cartAddRequestDto.setProductId(1L);

        User user = new User("test@naver.com", "asdf", null, "name", Role.USER);

        Category category = Category.builder().name("category").build();
        Product product = Product.builder()
                .price(new BigDecimal(100))
                .stock(1111)
                .description("desc")
                .seller(user)
                .name("productName")
                .category(category)
                .build();
        //when
        Cart entity = cartMapper.toEntity(cartAddRequestDto, user, product);

        //then
        assertThat(entity.getId()).isEqualTo(null);
        assertThat(entity.getQuantity()).isEqualTo(100);

        assertThat(entity.getUser()).isNotNull();
        assertThat(entity.getUser().getId()).isEqualTo(user.getId());
        assertThat(entity.getUser().getName()).isEqualTo(user.getName());
        assertThat(entity.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(entity.getUser().getPasswordHash()).isEqualTo(user.getPasswordHash());

        assertThat(entity.getProduct()).isNotNull();
        assertThat(entity.getProduct().getSeller().getId()).isEqualTo(product.getSeller().getId());
        assertThat(entity.getProduct().getDescription()).isEqualTo(product.getDescription());
        assertThat(entity.getProduct().getPrice()).isEqualTo(product.getPrice());
        assertThat(entity.getProduct().getStock()).isEqualTo(product.getStock());
        assertThat(entity.getProduct().getName()).isEqualTo(product.getName());
        assertThat(entity.getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void toDto() {
        //given
        User user = new User("test@naver.com", "asdf", null, "name", Role.USER);

        Category category = Category.builder().name("category").build();
        Product product = Product.builder()
                .price(new BigDecimal(100))
                .stock(1111)
                .description("desc")
                .seller(user)
                .name("productName")
                .category(category)
                .build();

        Cart cart = Cart.builder().quantity(100).product(product).user(user).build();

        //when
        CartDto dto = cartMapper.toDto(cart);

        //then
        assertThat(dto.getId()).isEqualTo(cart.getId());
        assertThat(dto.getQuantity()).isEqualTo(cart.getQuantity());

        assertThat(dto.getProduct()).isNotNull();
        assertThat(dto.getProduct().getId()).isEqualTo(product.getId());
        assertThat(dto.getProduct().getSellerName()).isEqualTo(product.getSeller().getName());
        assertThat(dto.getProduct().getCategoryName()).isEqualTo(product.getCategory().getName());
        assertThat(dto.getProduct().getPrice()).isEqualTo(product.getPrice());
        assertThat(dto.getProduct().getName()).isEqualTo(product.getName());
        assertThat(dto.getProduct().getStock()).isEqualTo(product.getStock());
    }
}