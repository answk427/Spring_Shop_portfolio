package work.trade.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.request.CartUpdateRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.product.domain.Category;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.service.ProductService;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.service.UserService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class CartServiceImplTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

//******************************//
    @Autowired private CartService cartService;
    @Autowired private UserService userService;
    @Autowired private ProductService productService;
    @Autowired private CategoryRepository categoryRepository;

//******************************//

    private Long userId;
    private Long productId1;
    private Long productId2;

    @BeforeEach
    @Transactional
    void Init() {
        UserCreateRequestDto userCreateDto = new UserCreateRequestDto();
        userCreateDto.setName("testUser");
        userCreateDto.setPassword("12341414");
        userCreateDto.setEmail("test@naver.com");
        UserDto userDto = userService.createUser(userCreateDto);
        userId = userDto.getId();

        Category testCategory = Category.builder().name("testCategory").build();
        Category category = categoryRepository.save(testCategory);

        ProductCreateRequestDto productCreateDto = new ProductCreateRequestDto();
        productCreateDto.setName("product");
        productCreateDto.setPrice(new BigDecimal(111111));
        productCreateDto.setStock(1234566);
        productCreateDto.setCategoryId(category.getId());
        ProductDto product = productService.createProduct(productCreateDto, userId);
        productId1 = product.getId();

        ProductCreateRequestDto productCreateDto2 = new ProductCreateRequestDto();
        productCreateDto2.setName("product2");
        productCreateDto2.setPrice(new BigDecimal(111111));
        productCreateDto2.setStock(1234566);
        productCreateDto2.setCategoryId(category.getId());
        ProductDto product2 = productService.createProduct(productCreateDto2, userId);
        productId2 = product2.getId();
    }

//******************************//


    @Test
    void addToCart() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto();
        cartAddRequestDto.setProductId(productId1);
        cartAddRequestDto.setQuantity(100);

        //when(기존에 없던 상품 추가)
        CartDto cartDto = cartService.addToCart(cartAddRequestDto, userId);
        Long cartId = cartDto.getId();

        //then
        assertThat(cartDto.getProduct().getId()).isEqualTo(productId1);
        assertThat(cartDto.getQuantity()).isEqualTo(cartAddRequestDto.getQuantity());

        //when(기존 상품에 수량 추가)
        CartDto cartDto2 = cartService.addToCart(cartAddRequestDto, userId);

        //then
        assertThat(cartDto2.getProduct().getId()).isEqualTo(productId1);
        assertThat(cartDto2.getQuantity()).isEqualTo(cartAddRequestDto.getQuantity() * 2);
    }

    @Test
    void getMyCart() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto();
        cartAddRequestDto.setProductId(productId1);
        cartAddRequestDto.setQuantity(100);

        CartAddRequestDto cartAddRequestDto2 = new CartAddRequestDto();
        cartAddRequestDto2.setProductId(productId2);
        cartAddRequestDto2.setQuantity(100);

        CartDto cartDto1 = cartService.addToCart(cartAddRequestDto, userId);
        CartDto cartDto2 = cartService.addToCart(cartAddRequestDto2, userId);

        //when
        List<CartDto> myCart = cartService.getMyCart(userId);
        CartDto myCartDto1 = myCart.get(0);
        CartDto myCartDto2 = myCart.get(1);

        //then
        assertThat(myCart.size()).isEqualTo(2);

        assertThat(myCartDto1.getId()).isEqualTo(cartDto1.getId());
        assertThat(myCartDto1.getProduct().getId()).isEqualTo(cartDto1.getProduct().getId());
        assertThat(myCartDto1.getQuantity()).isEqualTo(cartDto1.getQuantity());

        assertThat(myCartDto2.getId()).isEqualTo(cartDto2.getId());
        assertThat(myCartDto2.getProduct().getId()).isEqualTo(cartDto2.getProduct().getId());
        assertThat(myCartDto2.getQuantity()).isEqualTo(cartDto2.getQuantity());

    }

    @Test
    void updateQuantity() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto();
        cartAddRequestDto.setProductId(productId1);
        cartAddRequestDto.setQuantity(100);
        CartDto createdCartDto = cartService.addToCart(cartAddRequestDto, userId);

        CartUpdateRequestDto cartUpdateRequestDto = new CartUpdateRequestDto();
        cartUpdateRequestDto.setQuantity(4444);

        //when
        CartDto updatedCartDto = cartService.updateQuantity(cartUpdateRequestDto, createdCartDto.getId(), userId);

        //then
        assertThat(updatedCartDto.getQuantity()).isEqualTo(4444);
    }

    @Test
    void deleteCartItem() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto();
        cartAddRequestDto.setProductId(productId1);
        cartAddRequestDto.setQuantity(100);
        CartDto createdCartDto = cartService.addToCart(cartAddRequestDto, userId);

        //when
        cartService.deleteCartItem(createdCartDto.getId(), userId);

        //then
        List<CartDto> myCart = cartService.getMyCart(userId);

        assertThat(myCart).isEmpty();
    }

    @Test
    void deleteAllCartItems() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto();
        cartAddRequestDto.setProductId(productId1);
        cartAddRequestDto.setQuantity(100);

        CartAddRequestDto cartAddRequestDto2 = new CartAddRequestDto();
        cartAddRequestDto2.setProductId(productId2);
        cartAddRequestDto2.setQuantity(100);

        CartDto cartDto1 = cartService.addToCart(cartAddRequestDto, userId);
        CartDto cartDto2 = cartService.addToCart(cartAddRequestDto2, userId);

        //when
        cartService.deleteAllCartItems(userId);

        //then
        List<CartDto> myCart = cartService.getMyCart(userId);
        assertThat(myCart).isEmpty();
    }
}