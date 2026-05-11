package work.trade.product.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductImageDto;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.service.ProductImageService;
import work.trade.product.service.ProductService;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.service.UserService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class productControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

//*******************************//

    private Long testUserId;
    private String testUserToken;
    @Autowired
    private ProductImageService productImageService;

    @BeforeEach
    void InitData() {
        // 테스트용 판매자 생성
        UserCreateRequestDto dto = new UserCreateRequestDto("testEmail", "testPassword", "testSeller", null);
        UserDto testSeller = userService.createUser(dto);
        testUserId = testSeller.id();

        //테스트용 토큰 생성
        testUserToken = jwtTokenUtil.createAccessToken(testUserId.toString(), List.of(Role.USER));
    }

    private ProductCreateRequestDto getProductCreateRequestDto() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto(
                1L, "testProductName", "testDescription", BigDecimal.valueOf(111), 11234);
        return dto;
    }

    private ProductCreateRequestDto getProductCreateRequestDto(Long categoryId, String name, BigDecimal price) {
        ProductCreateRequestDto dto = new ProductCreateRequestDto(categoryId, name, "설명", price, 10);
        return dto;
    }

    private MockMultipartFile getMockMultiPartImage(String matchName, String imgFileName) {
        // 컨트롤러의 @RequestPart(value = "newImages")와 매칭
        return new MockMultipartFile(
                matchName,           // 컨트롤러의 @RequestPart(value = "newImages")와 매칭
                imgFileName,
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
    }

    private MockMultipartFile getMockMultiPartDto(String dtoString) {
        // 컨트롤러의 @RequestPart(value = "dto")와 매칭
        return new MockMultipartFile(
                "dto",
                "dto",
                MediaType.APPLICATION_JSON_VALUE,
                dtoString.getBytes(StandardCharsets.UTF_8)
        );
    }

//*******************************//

    @Test
    @DisplayName("판매 상품 생성 - POST /api/products")
    void createProduct() throws Exception {
        //given
        //image 파트 생성
        MockMultipartFile mockMultiPartImage1 = getMockMultiPartImage("newImages", "test_image.jpg");
        MockMultipartFile mockMultiPartImage2 = getMockMultiPartImage("newImages", "test_image2.jpg");
        MockMultipartFile mockMultiPartThumbnail = getMockMultiPartImage("thumbnail", "test_thumbnail.jpg");

        //JSON 파트 생성
        ProductCreateRequestDto createRequestDto = getProductCreateRequestDto();
        String dtoString = objectMapper.writeValueAsString(createRequestDto);
        MockMultipartFile dtoPart = getMockMultiPartDto(dtoString);

        //when, then
        //토큰 없을 시 인증 실패
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/products")
                        .file(dtoPart)
                        .file(mockMultiPartImage1)
                        .file(mockMultiPartImage2)
                        .file(mockMultiPartThumbnail)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        //토큰 인증 성공
        MvcResult result = mockMvc.perform(multipart( "/api/products")
                        .file(dtoPart)
                        .file(mockMultiPartImage1)
                        .file(mockMultiPartImage2)
                        .file(mockMultiPartThumbnail)
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("testProductName"))
                .andExpect(jsonPath("$.price").value(111))
                .andExpect(jsonPath("$.seller.id").value(testUserId))
                // 썸네일 검증
                .andExpect(jsonPath("$.thumbnail.imageUrl").value(Matchers.containsString("test_thumbnail.jpg")))
                .andReturn();

        //생성 확인
        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long createdId = jsonNode.get("id").asLong();

        Assertions.assertThat(productService.findProduct(createdId)).isNotNull();
    }

    @Test
    @DisplayName("상품 조회(로그인 필요X) - GET /api/products/{id}")
    void getProduct() throws Exception {
        //given
        MockMultipartFile mockMultiPartImage1 = getMockMultiPartImage("newImages", "getProductImage.jpg");
        MockMultipartFile mockMultiPartImage2 = getMockMultiPartImage("newImages", "getProductImage22222.jpg");
        MockMultipartFile mockMultiPartThumbnail = getMockMultiPartImage("thumbnail", "test_imageMyThumbnail.jpg");

        ProductCreateRequestDto dto = getProductCreateRequestDto();
        ProductDto product = productService.createProduct(dto, testUserId,
                mockMultiPartThumbnail, List.of(mockMultiPartImage1, mockMultiPartImage2));

        //when, then
        MvcResult result = mockMvc.perform(get("/api/products/" + product.id().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testProductName"))
                .andExpect(jsonPath("$.price").value(111))
                .andExpect(jsonPath("$.seller.id").value(testUserId))
                .andExpect(jsonPath("$.thumbnail.imageUrl").value(Matchers.containsString("Thumbnail.jpg")))
                .andReturn();

        //생성 확인
        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
    }

    @Test
    @DisplayName("상품 수정 - PUT /api/products/{id}")
    void updateProduct() throws Exception {
        // 1. Given: 수정할 데이터 준비 (DTO)
        MockMultipartFile mockMultiPartImage1 = getMockMultiPartImage("newImages", "test_image1.jpg");
        MockMultipartFile mockMultiPartImage2 = getMockMultiPartImage("newImages", "test_image2.jpg");
        MockMultipartFile mockMultiPartThumbnail = getMockMultiPartImage("thumbnail", "test_old_thumbnail.jpg");
        List<MultipartFile> newImages = List.of(mockMultiPartImage1, mockMultiPartImage2);

        ProductCreateRequestDto productCreateRequestDto = getProductCreateRequestDto();
        ProductDto product = productService.createProduct(productCreateRequestDto, testUserId, mockMultiPartThumbnail, newImages);

        //삭제할 이미지들의 id 목록
        List<Long> deleteImageIds = productImageService.getDetailImagesList(product.id())
                .stream().map(ProductImageDto::id).collect(Collectors.toList());

        if (product.thumbnail() != null) {
            deleteImageIds.add(product.thumbnail().id());
        }

        ProductUpdateDto updateDto = new ProductUpdateDto(
                2L, "updateName", "updateDescription",
                null, null, deleteImageIds);

        // DTO를 JSON 문자열로 변환
        String dtoString = objectMapper.writeValueAsString(updateDto);

        // JSON 파트 생성 (MockMultipartFile 이용)
        MockMultipartFile dtoPart = getMockMultiPartDto(dtoString);

        // 이미지 파일 파트 생성
        MockMultipartFile newImagePart = getMockMultiPartImage("newImages", "test_image3.jpg");
        MockMultipartFile newThumbnail = getMockMultiPartImage("thumbnail", "test_newThumbnail.jpg");

        // 2. When & Then: 요청 보내기
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/products/{productId}", 1L) // PATCH 메서드 명시
                        .file(dtoPart)
                        .file(newImagePart)
                        .file(newThumbnail)
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updateName"))
                .andExpect(jsonPath("$.description").value("updateDescription"))
                .andExpect(jsonPath("$.price").value(product.price().doubleValue()))
                .andExpect(jsonPath("$.stock").value(product.stock()))
                .andExpect(jsonPath("$.seller.id").value(testUserId))
                // 썸네일 검증
                .andExpect(jsonPath("$.thumbnail.imageUrl").value(Matchers.containsString("newThumbnail.jpg")));
    }

    @Test
    @DisplayName("상품 삭제 - DELETE /api/products/{id}")
    void deleteProduct() throws Exception {
        //given
        ProductCreateRequestDto productCreateRequestDto = getProductCreateRequestDto();
        ProductDto product = productService.createProduct(productCreateRequestDto, testUserId);

        ProductUpdateDto updateDto = new ProductUpdateDto(null, "updateName", "updateDescription", null, null, null);

        //when, then
        mockMvc.perform(delete("/api/products/" + product.id().toString())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        //삭제 후 조회시 예외
        Assertions.assertThatThrownBy(() -> productService.findProduct(product.id()));
    }

    @Test
    @DisplayName("상품 목록 조회(로그인 필요X) - GET /api/products")
    void getProducts() throws Exception {
        // given - 상품 몇 개 생성
        productService.createProduct(getProductCreateRequestDto(1L, "상품1", new BigDecimal("1000")), testUserId);
        productService.createProduct(getProductCreateRequestDto(1L, "상품2", new BigDecimal("2000")), testUserId);
        productService.createProduct(getProductCreateRequestDto(1L, "상품3", new BigDecimal("3000")), testUserId);

        // when, then
        // 토큰 없이도 조회 가능
        mockMvc.perform(get("/api/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));

        // 페이징 파라미터 적용
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))  // size=2 적용됨
                .andExpect(jsonPath("$.totalElements").value(3))      // 전체는 3개
                .andExpect(jsonPath("$.totalPages").value(2))         // 2페이지
                .andExpect(jsonPath("$.last").value(false));          // 마지막 페이지 아님

        // 2페이지
        mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))  // 마지막 1개
                .andExpect(jsonPath("$.last").value(true));           // 마지막 페이지
    }

    @Test
    @DisplayName("카테고리별 상품 목록 조회(로그인 필요X) - GET /api/products/category/{id}")
    void getProductsByCategory() throws Exception {
        // 카테고리1에 상품 2개
        productService.createProduct(getProductCreateRequestDto(1L, "카테고리1 상품1", new BigDecimal("1000")), testUserId);
        productService.createProduct(getProductCreateRequestDto(1L, "카테고리1 상품2", new BigDecimal("2000")), testUserId);

        // 카테고리2에 상품 1개
        ProductCreateRequestDto dto = getProductCreateRequestDto(2L, "카테고리2 상품1", new BigDecimal("3000"));
        productService.createProduct(dto, testUserId);

        // when, then
        // testCategoryId로 조회 → 2개만 나와야 함
        mockMvc.perform(get("/api/products/category/" + 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // category2로 조회 → 1개만 나와야 함
        mockMvc.perform(get("/api/products/category/" + 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        // 존재하지 않는 카테고리 → 404
        mockMvc.perform(get("/api/products/category/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내가 등록한 상품 조회 - GET /api/products/my")
    void getMyProducts() throws Exception {
        // given - testUser 상품 2개, otherUser 상품 1개 생성
        productService.createProduct(getProductCreateRequestDto(
                1L, "내 상품1", new BigDecimal("1000")), testUserId);
        productService.createProduct(getProductCreateRequestDto(
                1L, "내 상품2", new BigDecimal("2000")), testUserId);

        // otherUser 생성
        UserCreateRequestDto otherUserDto = new UserCreateRequestDto(
                "other@test.com", "password123", "다른유저", null);
        UserDto otherUser = userService.createUser(otherUserDto);
        String otherUserToken = jwtTokenUtil.createAccessToken(otherUser.id().toString(), List.of(Role.USER));
        productService.createProduct(getProductCreateRequestDto(
                1L, "다른유저 상품", new BigDecimal("3000")), otherUser.id());

        // when, then
        // 토큰 없이 → 401
        mockMvc.perform(get("/api/products/my"))
                .andExpect(status().isUnauthorized());

        // testUser 토큰으로 조회 → 내 상품 2개만
        mockMvc.perform(get("/api/products/my")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // otherUser 토큰으로 조회 → 다른유저 상품 1개만
        mockMvc.perform(get("/api/products/my")
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}