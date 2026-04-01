package work.trade.product.service.container;

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
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.CategoryDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.product.service.ProductService;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.SellerDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.service.UserService;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class ProductServiceImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;

    //----------
    public static String testSellerName = "testSeller";
    public static String testSellerEmail = "seller@test.com";
    public static String testSellerPassword = "passwordHash";
    public static String testCategoryName = "Test Category";
    public static String testCategoryName2 = "Test Category2";

    private Long testUserId = 0L;
    private Long testCategoryId = 0L;
    private Long testCategoryId2 = 0L;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

//------------------------------------------------------------------//
    @BeforeEach
    @Transactional
    void InitData() {
        // 테스트용 판매자 생성
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setName(testSellerName);
        dto.setEmail(testSellerEmail);
        dto.setPassword(testSellerPassword);
        UserDto testSeller = userService.createUser(dto);
        testUserId = testSeller.getId();

        // 테스트용 카테고리 생성
        Category testCategory = Category.builder()
                .name(testCategoryName)
                .build();
        categoryRepository.save(testCategory);
        testCategoryId = testCategory.getId();

        Category testCategory2 = Category.builder()
                .name(testCategoryName2)
                .build();
        categoryRepository.save(testCategory2);
        testCategoryId2 = testCategory2.getId();
    }

    void verifySeller(User seller) {
        //Entity Seller 검증
        assertThat(seller.getId()).isEqualTo(testUserId);
        assertThat(seller.getName()).isEqualTo(testSellerName);
        assertThat(seller.getEmail()).isEqualTo(testSellerEmail);
    }

    void verifyCategory(Category category) {
        //Entity category 검증
        assertThat(category.getId()).isEqualTo(testCategoryId);
        assertThat(category.getName()).isEqualTo(testCategoryName);
        assertThat(category.getParent()).isNull();
    }

    @Transactional
    private ProductDto createTestProduct(String name, BigDecimal price, int stock, String description) {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setCategoryId(testCategoryId);
        dto.setName(name);
        dto.setPrice(price);
        dto.setStock(stock);
        dto.setDescription(description);

        ProductDto productDto = productService.createProduct(dto, testUserId);
        em.flush(); // DB에 반영
        em.clear(); // 1차 캐시 비우기 (실제 DB 조회 테스트를 위해)

        return productDto;
    }
//------------------------------------------------------------------//
    @Test
    @Transactional
    void createProduct() {
        //given
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setCategoryId(testCategoryId);
        final String productName = "Test Product";
        final BigDecimal productPrice = BigDecimal.valueOf(12121);
        final int productStock = 12123;
        final String productDescription = "테스트 제품 설명";


        //when
        ProductDto createProduct = createTestProduct(productName, productPrice, productStock, productDescription);

        //then
        //-----------------Repository로 얻은 Entity 검증
        Optional<Product> productByRepoOpt = productRepository.findById(createProduct.getId());
        assertThat(productByRepoOpt.isPresent()).isTrue();
        Product productByRepo = productByRepoOpt.get();

        assertThat(productByRepo.getName()).isEqualTo(productName);
        assertThat(productByRepo.getPrice()).isEqualByComparingTo(productPrice);
        assertThat(productByRepo.getStock()).isEqualTo(productStock);
        assertThat(productByRepo.getDescription()).isEqualTo(productDescription);
        assertThat(productByRepo.getCreatedAt()).isNotNull();
        assertThat(productByRepo.getUpdatedAt()).isNotNull();

        assertThat(productByRepo.getSeller()).isNotNull();
        verifySeller(productByRepo.getSeller());

        assertThat(productByRepo.getCategory()).isNotNull();
        verifyCategory(productByRepo.getCategory());

        //-----------------Service로 얻은 Dto 검증
        ProductDto productDto = productService.findProduct(createProduct.getId());

        assertThat(productDto.getId()).isEqualTo(productByRepo.getId());
        assertThat(productDto.getCreatedAt()).isEqualTo(productByRepo.getCreatedAt());
        assertThat(productDto.getUpdatedAt()).isEqualTo(productByRepo.getUpdatedAt());
        assertThat(productDto.getName()).isEqualTo(productByRepo.getName());
        assertThat(productDto.getPrice()).isEqualTo(productByRepo.getPrice());
        assertThat(productDto.getStock()).isEqualTo(productByRepo.getStock());
        assertThat(productDto.getDescription()).isEqualTo(productByRepo.getDescription());

        //Dto Seller 검증
        assertThat(productDto.getSeller()).isNotNull();
        SellerDto dtoSeller = productDto.getSeller();
        assertThat(dtoSeller.getId()).isEqualTo(productByRepo.getSeller().getId());
        assertThat(dtoSeller.getEmail()).isEqualTo(productByRepo.getSeller().getEmail());
        assertThat(dtoSeller.getName()).isEqualTo(productByRepo.getSeller().getName());

        //Dto Category 검증
        assertThat(productDto.getCategory()).isNotNull();
        CategoryDto dtoCategory = productDto.getCategory();
        assertThat(dtoCategory.getId()).isEqualTo(productByRepo.getCategory().getId());
        assertThat(dtoCategory.getName()).isEqualTo(productByRepo.getCategory().getName());

        Category parent = productByRepo.getCategory().getParent();
        if (parent != null) {
            assertThat(dtoCategory.getParentId()).isEqualTo(parent.getId());
            assertThat(dtoCategory.getParentName()).isEqualTo(parent.getName());
        }
    }

    @Test
    @Transactional
    void findProduct() {
        // given
        final String productName = "Find Test Product";
        final BigDecimal productPrice = BigDecimal.valueOf(9900);
        final int productStock = 50;
        final String productDescription = "조회 테스트 제품 설명";

        ProductDto createProduct = createTestProduct(productName, productPrice, productStock, productDescription);

        //when
        ProductDto productDto = productService.findProduct(createProduct.getId());

        //then
        //-----------------Service로 얻은 Dto 검증
        assertThat(productDto.getId()).isEqualTo(createProduct.getId());
        assertThat(productDto.getName()).isEqualTo(productName);
        assertThat(productDto.getPrice()).isEqualByComparingTo(productPrice);
        assertThat(productDto.getStock()).isEqualTo(productStock);
        assertThat(productDto.getDescription()).isEqualTo(productDescription);

        assertThat(productDto.getCreatedAt()).isNotNull();
        assertThat(productDto.getUpdatedAt()).isNotNull();

        //Dto Seller 검증
        assertThat(productDto.getSeller()).isNotNull();
        SellerDto dtoSeller = productDto.getSeller();
        assertThat(dtoSeller.getId()).isEqualTo(createProduct.getSeller().getId());
        assertThat(dtoSeller.getEmail()).isEqualTo(createProduct.getSeller().getEmail());
        assertThat(dtoSeller.getName()).isEqualTo(createProduct.getSeller().getName());

        //Dto Category 검증
        assertThat(productDto.getCategory()).isNotNull();
        CategoryDto dtoCategory = productDto.getCategory();
        assertThat(dtoCategory.getId()).isEqualTo(createProduct.getCategory().getId());
        assertThat(dtoCategory.getName()).isEqualTo(createProduct.getCategory().getName());

        assertThat(dtoCategory.getParentId()).isEqualTo(productDto.getCategory().getParentId());
        assertThat(dtoCategory.getParentName()).isEqualTo(productDto.getCategory().getParentName());

        //존재하지 않는 ID 조회 테스트
        assertThatThrownBy(()->productService.findProduct(99999L));
    }

    @Test
    @Transactional
    void updateProduct() {
        //given
        final String originalName = "Original Name";
        final BigDecimal originalPrice = BigDecimal.valueOf(1000);
        final int originalStock = 1111;
        final String originalDesc = "Original Description";
        ProductDto createProduct = createTestProduct(originalName, originalPrice, originalStock, originalDesc);
        ProductDto product = productService.findProduct(createProduct.getId());

        final String updateName = "Updated Name";
        final BigDecimal updatePrice = BigDecimal.valueOf(33000);
        final int updateStock = 2222;
        final String updateDesc = "Update Description";

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setId(product.getId());
        updateDto.setCategoryId(testCategoryId2);
        updateDto.setName(updateName);
        updateDto.setPrice(updatePrice);
        updateDto.setStock(updateStock);
        updateDto.setDescription(updateDesc);

        //when
        ProductDto updatedDto = productService.updateProduct(updateDto, testUserId);
        em.flush();
        em.clear();

        //then
        //DTO 검증
        assertThat(updatedDto.getId()).isEqualTo(product.getId());
        assertThat(updatedDto.getName()).isEqualTo(updateName);
        assertThat(updatedDto.getPrice()).isEqualByComparingTo(updatePrice);
        assertThat(updatedDto.getStock()).isEqualTo(updateStock);
        assertThat(updatedDto.getDescription()).isEqualTo(updateDesc);
        // 업데이트 시간 검증: 업데이트 이전 시간보다 이후여야 함
        assertThat(updatedDto.getUpdatedAt()).isAfterOrEqualTo(product.getUpdatedAt());
        // 생성 시간은 변경되지 않아야 함
        assertThat(updatedDto.getCreatedAt()).isCloseTo(product.getCreatedAt(), within(1, ChronoUnit.MILLIS));

        //Repository로 조회한 Entity 검증 (DB 반영 확인)
        Optional<Product> productByRepoOpt = productRepository.findById(product.getId());
        assertThat(productByRepoOpt.isPresent()).isTrue();
        Product productByRepo = productByRepoOpt.get();

        assertThat(productByRepo.getName()).isEqualTo(updateName);
        assertThat(productByRepo.getPrice()).isEqualByComparingTo(updatePrice);
        assertThat(productByRepo.getStock()).isEqualTo(updateStock);
        assertThat(productByRepo.getDescription()).isEqualTo(updateDesc);

        //잘못된 id를 update할 경우
        updateDto.setId(1123123L);
        assertThatThrownBy(()->productService.updateProduct(updateDto,testUserId));
    }

    @Test
    @Transactional
    void deleteById() {
        //given
        final String name = "delete Name";
        final BigDecimal price = BigDecimal.valueOf(1000);
        final int stock = 1111;
        final String description = "delete Description";
        ProductDto productDto = createTestProduct(name, price, stock, description);

        //when
        productService.deleteById(productDto.getId(), testUserId);

        //then
        assertThatThrownBy(()->productService.findProduct(productDto.getId()));
    }
}