package work.trade.cart.service;

import jakarta.persistence.EntityManager;
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
import work.trade.cart.dto.response.CartItemDto;
import work.trade.product.dto.request.ProductCreateRequestDto;
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

    @Autowired private EntityManager em;

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
        UserCreateRequestDto userCreateDto = new UserCreateRequestDto(
                "testUser", "12341414", "test@naver.com", null);
        UserDto userDto = userService.createUser(userCreateDto);
        userId = userDto.id();

        ProductCreateRequestDto productCreateDto = new ProductCreateRequestDto(1L, "product", "productDesc", new BigDecimal(111111), 1234566);
        productId1 = productService.createProduct(productCreateDto, userId).id();

        ProductCreateRequestDto productCreateDto2 = new ProductCreateRequestDto(1L, "product2", "product2Desc", new BigDecimal(111111), 1234566);
        productId2 = productService.createProduct(productCreateDto2, userId).id();
    }

//******************************//

    @Test
    void addToCart() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto(productId1, 100);

        //N+1문제 확인 위해 영속성 컨텍스트 초기화
        em.clear();

        //when(기존에 없던 상품 추가)
        System.out.println("================= [로직 시작] =================");
        CartDto cartDto = cartService.addToCart(cartAddRequestDto, userId);
        System.out.println("================= [로직 종료] =================");

        //then
        assertThat(cartDto.product().id()).isEqualTo(productId1);
        assertThat(cartDto.quantity()).isEqualTo(cartAddRequestDto.getQuantity());

        //when(기존 상품에 수량 추가)
        CartDto cartDto2 = cartService.addToCart(cartAddRequestDto, userId);

        //then
        assertThat(cartDto2.product().id()).isEqualTo(productId1);
        assertThat(cartDto2.quantity()).isEqualTo(cartAddRequestDto.getQuantity() * 2);

        //[로직시작] [로직종료] 사이에 SQL로그 확인
        //올바른 유저인지 확인 SELECT USER
        //상품 재고 확인 SELECT PRODUCT
        //장바구니에 이미 존재하는지 확인 SELECT CART
        //장바구니에 추가 ADD CART
        //SQL 총 4번
    }

    @Test
    void getMyCart() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto(productId1, 100);
        CartAddRequestDto cartAddRequestDto2 = new CartAddRequestDto(productId2, 100);

        CartDto cartDto1 = cartService.addToCart(cartAddRequestDto, userId);
        CartDto cartDto2 = cartService.addToCart(cartAddRequestDto2, userId);

        //N+1문제 확인 위해 영속성 컨텍스트 초기화
        em.clear();

        //when
        System.out.println("================= [로직 시작] =================");
        List<CartDto> myCart = cartService.getMyCart(userId);
        System.out.println("================= [로직 종료] =================");

        CartDto myCartDto1 = myCart.get(0);
        CartDto myCartDto2 = myCart.get(1);

        //then
        assertThat(myCart.size()).isEqualTo(2);

        assertThat(myCartDto1.id()).isEqualTo(cartDto1.id());
        assertThat(myCartDto1.product().id()).isEqualTo(cartDto1.product().id());
        assertThat(myCartDto1.quantity()).isEqualTo(cartDto1.quantity());

        assertThat(myCartDto2.id()).isEqualTo(cartDto2.id());
        assertThat(myCartDto2.product().id()).isEqualTo(cartDto2.product().id());
        assertThat(myCartDto2.quantity()).isEqualTo(cartDto2.quantity());

        //[로직시작] [로직종료] 사이에 SQL로그 1번 확인
    }

    @Test
    void updateQuantity() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto(productId1, 100);
        CartDto createdCartDto = cartService.addToCart(cartAddRequestDto, userId);

        CartUpdateRequestDto cartUpdateRequestDto = new CartUpdateRequestDto(4444);

        //N+1문제 확인 위해 영속성 컨텍스트 초기화
        em.clear();

        //when
        System.out.println("================= [로직 시작] =================");
        CartDto updatedCartDto = cartService.updateQuantity(cartUpdateRequestDto, createdCartDto.id(), userId);
        System.out.println("================= [로직 종료] =================");

        //then
        assertThat(updatedCartDto.quantity()).isEqualTo(4444);

        //[로직시작] [로직종료] 사이에 SQL로그 1번 확인
    }

    @Test
    void deleteCartItem() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto(productId1, 100);
        CartDto createdCartDto = cartService.addToCart(cartAddRequestDto, userId);

        //N+1문제 확인 위해 영속성 컨텍스트 초기화
        em.clear();

        //when
        System.out.println("================= [로직 시작] =================");
        cartService.deleteCartItem(createdCartDto.id(), userId);
        System.out.println("================= [로직 종료] =================");

        //then
        List<CartDto> myCart = cartService.getMyCart(userId);

        assertThat(myCart).isEmpty();

        //[로직시작] [로직종료] 사이에 select SQL로그 1번 확인
        //flush시 delete sql로그 1번 확인

    }

    @Test
    void deleteAllCartItems() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto(productId1, 100);

        CartAddRequestDto cartAddRequestDto2 = new CartAddRequestDto(productId2, 100);

        CartDto cartDto1 = cartService.addToCart(cartAddRequestDto, userId);
        CartDto cartDto2 = cartService.addToCart(cartAddRequestDto2, userId);

        //when
        System.out.println("================= [로직 시작] =================");
        cartService.deleteAllCartItems(userId);
        System.out.println("================= [로직 종료] =================");

        //then
        List<CartDto> myCart = cartService.getMyCart(userId);
        assertThat(myCart).isEmpty();
    }

    @Test
    void getCartItemIdsForOrder() {
        //given
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto(productId1, 100);
        CartAddRequestDto cartAddRequestDto2 = new CartAddRequestDto(productId2, 100);

        CartDto cartDto1 = cartService.addToCart(cartAddRequestDto, userId);
        CartDto cartDto2 = cartService.addToCart(cartAddRequestDto2, userId);

        //when
        System.out.println("================= [로직 시작] =================");
        List<CartItemDto> cartItems = cartService.getCartItemIdsForOrder(userId);
        System.out.println("================= [로직 종료] =================");

        //then
        assertThat(cartItems.size()).isEqualTo(2);

        assertThat(cartItems.get(0).productId()).isEqualTo(cartDto1.product().id());
        assertThat(cartItems.get(0).quantity()).isEqualTo(cartDto1.quantity());

        assertThat(cartItems.get(1).productId()).isEqualTo(cartDto2.product().id());
        assertThat(cartItems.get(1).quantity()).isEqualTo(cartDto2.quantity());

        //[로직시작] [로직종료] 사이에 select SQL로그 1번 확인
    }
}