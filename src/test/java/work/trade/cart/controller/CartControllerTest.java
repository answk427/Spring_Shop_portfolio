package work.trade.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.request.CartUpdateRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.cart.repository.CartRepository;
import work.trade.cart.service.CartService;
import work.trade.product.domain.Category;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.product.service.ProductService;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.repository.UserRepository;
import work.trade.user.service.UserService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class CartControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenUtil jwtTokenUtil;

//******************************//
    @Autowired private CartService cartService;
    @Autowired private UserService userService;
    @Autowired private ProductService productService;

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    //******************************//
    private String testUserToken;
    private Long userId;
    private Long productId1;
    private Long productId2;

    private MvcResult RequestAddCart(Long productId, Integer quantity) throws Exception {
        CartAddRequestDto dto = new CartAddRequestDto();
        dto.setProductId(productId);
        dto.setQuantity(quantity);

        return mockMvc.perform(post("/carts")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @BeforeEach
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

        //테스트용 토큰 생성
        testUserToken = jwtTokenUtil.createAccessToken(userId.toString(), List.of(Role.USER));
    }

//******************************//

    @Test
    @Transactional
    void addToCart() throws Exception {
        RequestAddCart(productId1, 100);
    }

    @Test
    @Transactional
    void getMyCart() throws Exception {
        //given
        //장바구니에 2개 생성
        MvcResult result1 = RequestAddCart(productId1, 100);
        MvcResult result2 = RequestAddCart(productId2, 200);

        String responseBody1 = result1.getResponse().getContentAsString();
        String responseBody2 = result2.getResponse().getContentAsString();

        CartDto cartDto1 = objectMapper.readValue(responseBody1, CartDto.class);
        CartDto cartDto2 = objectMapper.readValue(responseBody2, CartDto.class);
        List<CartDto> carts = List.of(cartDto1, cartDto2);

        ProductSummaryDto product1 = cartDto1.getProduct();
        ProductSummaryDto product2 = cartDto2.getProduct();
        List<ProductSummaryDto> products = List.of(product1, product2);

        //when, then
        ResultActions result = mockMvc.perform(get("/carts")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk());

        for (int i = 0; i < 2; ++i) {
            result.andExpect(jsonPath("$[%d].product.id", i).value(products.get(i).getId()))
                    .andExpect(jsonPath("$[%d].product.name", i).value(products.get(i).getName()))
                    .andExpect(jsonPath("$[%d].product.price", i).value(products.get(i).getPrice().doubleValue()))
                    .andExpect(jsonPath("$[%d].product.stock", i).value(products.get(i).getStock()))
                    .andExpect(jsonPath("$[%d].product.sellerName", i).value(products.get(i).getSellerName()))
                    .andExpect(jsonPath("$[%d].product.categoryName", i).value(products.get(i).getCategoryName()))
                    .andExpect(jsonPath("$[%d].id", i).value(carts.get(i).getId()))
                    .andExpect(jsonPath("$[%d].quantity", i).value(carts.get(i).getQuantity()));
        }
    }

    @Test
    void updateQuantity() throws Exception {
        //given
        MvcResult result = RequestAddCart(productId1, 100);
        String responseBody = result.getResponse().getContentAsString();
        CartDto cartDto = objectMapper.readValue(responseBody, CartDto.class);

        CartUpdateRequestDto cartUpdateRequestDto = new CartUpdateRequestDto();
        cartUpdateRequestDto.setQuantity(4444);

        //when, then
        mockMvc.perform(put("/carts/" + cartDto.getId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartUpdateRequestDto)))
                .andExpect(jsonPath("$.quantity").value(cartUpdateRequestDto.getQuantity()));
    }

    @Test
    void deleteCartItem() throws Exception {
        //given
        MvcResult result = RequestAddCart(productId1, 100);
        String responseBody = result.getResponse().getContentAsString();
        CartDto cartDto = objectMapper.readValue(responseBody, CartDto.class);

        //when, then
        mockMvc.perform(delete("/carts/" + cartDto.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        //조회시 empty 확인
        mockMvc.perform(get("/carts")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteAllCartItems() throws Exception {
        //given
        MvcResult result = RequestAddCart(productId1, 100);
        MvcResult result2 = RequestAddCart(productId2, 200);

        String responseBody = result.getResponse().getContentAsString();
        CartDto cartDto = objectMapper.readValue(responseBody, CartDto.class);

        String responseBody2 = result2.getResponse().getContentAsString();
        CartDto cartDto2 = objectMapper.readValue(responseBody2, CartDto.class);

        //when, then
        mockMvc.perform(delete("/carts")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        //조회시 empty 확인
        mockMvc.perform(get("/carts")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}