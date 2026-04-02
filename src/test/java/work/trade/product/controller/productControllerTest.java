package work.trade.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;
import work.trade.product.domain.Category;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.product.service.ProductService;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.service.UserService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class productControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final ProductService productService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public productControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, ProductService productService, UserService userService, CategoryRepository categoryRepository, JwtTokenUtil jwtTokenUtil) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

//**************************//
    Long testCategoryId;
    Long testUserId;
    String testUserToken;

//**************************//
    @BeforeEach
    void InitData() {
        // 테스트용 판매자 생성
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setName("testSeller");
        dto.setEmail("testEmail");
        dto.setPassword("testPassword");
        UserDto testSeller = userService.createUser(dto);
        testUserId = testSeller.getId();
        //테스트용 토큰 생성
        testUserToken = jwtTokenUtil.createToken(testUserId.toString(), List.of(Role.USER));

        // 테스트용 카테고리 생성
        Category testCategory = Category.builder()
                .name("testCategoryName")
                .build();
        categoryRepository.save(testCategory);
        testCategoryId = testCategory.getId();
    }

    private ProductCreateRequestDto getProductCreateRequestDto() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setName("testProductName");
        dto.setPrice(new BigDecimal(111));
        dto.setStock(11234);
        dto.setCategoryId(testCategoryId);
        dto.setDescription("testDescription");
        return dto;
    }

    private ProductCreateRequestDto getProductCreateRequestDto(String name, BigDecimal price) {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setCategoryId(testCategoryId);
        dto.setName(name);
        dto.setPrice(price);
        dto.setStock(10);
        dto.setDescription("설명");
        return dto;
    }
//**************************//
    @Test
    @DisplayName("상품 생성 요청")
    void createProduct() throws Exception {
        //given
        ProductCreateRequestDto dto = getProductCreateRequestDto();

        //when, then
        //토큰 없을 시 인증 실패
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

        //토큰 인증 성공
        MvcResult result = mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("testProductName"))
                .andExpect(jsonPath("$.price").value(111))
                .andExpect(jsonPath("$.seller.id").value(testUserId))
                .andReturn();

        //생성 확인
        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long createdId = jsonNode.get("id").asLong();

        Assertions.assertThat(productService.findProduct(createdId)).isNotNull();
    }

    @Test
    @DisplayName("상품 조회 - 로그인 필요X")
    void getProduct() throws Exception {
        //given
        ProductCreateRequestDto dto = getProductCreateRequestDto();
        ProductDto product = productService.createProduct(dto, testUserId);

        //when, then
        mockMvc.perform(get("/products/" + product.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testProductName"))
                .andExpect(jsonPath("$.price").value(111))
                .andExpect(jsonPath("$.seller.id").value(testUserId));
    }

    @Test
    void updateProduct() throws Exception {
        //given
        ProductCreateRequestDto productCreateRequestDto = getProductCreateRequestDto();
        ProductDto product = productService.createProduct(productCreateRequestDto, testUserId);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setName("updateName");
        updateDto.setDescription("updateDescription");

        //when, then
        mockMvc.perform(put("/products/" + product.getId().toString())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updateName"))
                .andExpect(jsonPath("$.description").value("updateDescription"))
                .andExpect(jsonPath("$.price").value(product.getPrice().doubleValue()))
                .andExpect(jsonPath("$.stock").value(product.getStock()))
                .andExpect(jsonPath("$.seller.id").value(testUserId));
    }

    @Test
    //@DeleteMapping("/{id}")
    void deleteProduct() throws Exception {
        //given
        ProductCreateRequestDto productCreateRequestDto = getProductCreateRequestDto();
        ProductDto product = productService.createProduct(productCreateRequestDto, testUserId);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setName("updateName");
        updateDto.setDescription("updateDescription");

        //when, then
        mockMvc.perform(delete("/products/" + product.getId().toString())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Assertions.assertThatThrownBy(() -> productService.findProduct(product.getId()));

    }

    @Test
    //@GetMapping("/products")
    void getProducts() throws Exception {
        // given - 상품 몇 개 생성
        productService.createProduct(getProductCreateRequestDto("상품1", new BigDecimal("1000")), testUserId);
        productService.createProduct(getProductCreateRequestDto("상품2", new BigDecimal("2000")), testUserId);
        productService.createProduct(getProductCreateRequestDto("상품3", new BigDecimal("3000")), testUserId);

        // when, then
        // 토큰 없이도 조회 가능
        mockMvc.perform(get("/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));

        // 페이징 파라미터 적용
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))  // size=2 적용됨
                .andExpect(jsonPath("$.totalElements").value(3))      // 전체는 3개
                .andExpect(jsonPath("$.totalPages").value(2))         // 2페이지
                .andExpect(jsonPath("$.last").value(false));          // 마지막 페이지 아님

        // 2페이지
        mockMvc.perform(get("/products")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))  // 마지막 1개
                .andExpect(jsonPath("$.last").value(true));           // 마지막 페이지
    }

    @Test
    //@GetMapping("/products/category/{categoryId}")
    void getProductsByCategory() throws Exception {
        // given - 카테고리 2개 생성
        Category category2 = Category.builder().name("다른카테고리").build();
        categoryRepository.save(category2);

        // testCategoryId에 상품 2개
        productService.createProduct(getProductCreateRequestDto("카테고리1 상품1", new BigDecimal("1000")), testUserId);
        productService.createProduct(getProductCreateRequestDto("카테고리1 상품2", new BigDecimal("2000")), testUserId);

        // category2에 상품 1개
        ProductCreateRequestDto dto = getProductCreateRequestDto("카테고리2 상품1", new BigDecimal("3000"));
        dto.setCategoryId(category2.getId());
        productService.createProduct(dto, testUserId);

        // when, then
        // testCategoryId로 조회 → 2개만 나와야 함
        mockMvc.perform(get("/products/category/" + testCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // category2로 조회 → 1개만 나와야 함
        mockMvc.perform(get("/products/category/" + category2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        // 존재하지 않는 카테고리 → 404
        mockMvc.perform(get("/products/category/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    //@GetMapping("/products/my")
    void getMyProducts() throws Exception {
        // given - testUser 상품 2개, otherUser 상품 1개 생성
        productService.createProduct(getProductCreateRequestDto("내 상품1", new BigDecimal("1000")), testUserId);
        productService.createProduct(getProductCreateRequestDto("내 상품2", new BigDecimal("2000")), testUserId);

        // otherUser 생성
        UserCreateRequestDto otherUserDto = new UserCreateRequestDto();
        otherUserDto.setEmail("other@test.com");
        otherUserDto.setPassword("password123");
        otherUserDto.setName("다른유저");
        UserDto otherUser = userService.createUser(otherUserDto);
        String otherUserToken = jwtTokenUtil.createToken(otherUser.getId().toString(), List.of(Role.USER));
        productService.createProduct(getProductCreateRequestDto("다른유저 상품", new BigDecimal("3000")), otherUser.getId());

        // when, then
        // 토큰 없이 → 401
        mockMvc.perform(get("/products/my"))
                .andExpect(status().isUnauthorized());

        // testUser 토큰으로 조회 → 내 상품 2개만
        mockMvc.perform(get("/products/my")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // otherUser 토큰으로 조회 → 다른유저 상품 1개만
        mockMvc.perform(get("/products/my")
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}