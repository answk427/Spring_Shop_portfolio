package work.trade.cart.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import work.trade.auth.role.Role;
import work.trade.cart.domain.Cart;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.service.ProductService;
import work.trade.user.domain.User;
import work.trade.user.service.UserService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CartMapperTest {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

//*******************************//

    User getUser() {
        return User.builder()
                .email("test@naver.com")
                .passwordHash("asdf")
                .authProvider(null)
                .name("name")
                .role(Role.USER)
                .build();
    }

    Product getProduct(User seller) {
        Category category = Category.builder().name("category").build();
        return Product.builder()
                .price(new BigDecimal(100))
                .stock(1111)
                .description("desc")
                .seller(seller)
                .name("productName")
                .category(category)
                .build();
    }


//*******************************//

    @Test
    void toEntity() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto(1L, 100);
        User user = getUser();
        Product product = getProduct(user);

        //when
        Cart entity = cartMapper.toEntity(cartAddRequestDto, user, product);

        //then
        assertThat(entity.getId()).isEqualTo(null);
        assertThat(entity.getQuantity()).isEqualTo(100);

        //Cart의 유저 정보 검증
        assertThat(entity.getUser()).isNotNull();
        assertThat(entity.getUser().getId()).isEqualTo(user.getId());
        assertThat(entity.getUser().getName()).isEqualTo(user.getName());
        assertThat(entity.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(entity.getUser().getPasswordHash()).isEqualTo(user.getPasswordHash());

        //Cart의 Product 정보 검증
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
        User user = getUser();
        Product product = getProduct(user);
        Cart cart = Cart.builder().quantity(100).product(product).user(user).build();

        //when
        CartDto dto = cartMapper.toDto(cart);

        //then
        assertThat(dto.id()).isEqualTo(cart.getId());
        assertThat(dto.quantity()).isEqualTo(cart.getQuantity());

        assertThat(dto.product()).isNotNull();
        assertThat(dto.product().id()).isEqualTo(product.getId());
        assertThat(dto.product().sellerName()).isEqualTo(product.getSeller().getName());
        assertThat(dto.product().categoryName()).isEqualTo(product.getCategory().getName());
        assertThat(dto.product().price()).isEqualTo(product.getPrice());
        assertThat(dto.product().name()).isEqualTo(product.getName());
        assertThat(dto.product().stock()).isEqualTo(product.getStock());
    }
}