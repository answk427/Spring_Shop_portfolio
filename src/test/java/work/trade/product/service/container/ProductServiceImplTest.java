package work.trade.product.service.container;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.product.service.ProductService;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.SellerDto;
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
    public static String testSellerEmail1 = "seller1@test.com";
    public static String testSellerPassword = "passwordHash";
    public static String testCategoryName = "전자제품";
    public static String testCategoryName1 = "스마트폰";

    private Long testUserId = 0L;
    private Long testUserId1 = 0L;
    private final Long testCategoryId = 1L;
    private final Long testCategoryId1 = 2L;

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
        UserCreateRequestDto dto = new UserCreateRequestDto(testSellerEmail, testSellerPassword, testSellerName, null);
        UserCreateRequestDto dto1 = new UserCreateRequestDto(testSellerEmail1, testSellerPassword, testSellerName + "2", null);
        testUserId = userService.createUser(dto).id();
        testUserId1 = userService.createUser(dto1).id();

        UserCreateRequestDto dto2 = new UserCreateRequestDto(testSellerEmail1, testSellerPassword, testSellerName+"1", null);
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
    private ProductDto createTestProduct(String name, BigDecimal price, int stock, Long categoryId, Long userId, String description) {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setCategoryId(categoryId);
        dto.setName(name);
        dto.setPrice(price);
        dto.setStock(stock);
        dto.setDescription(description);

        ProductDto productDto = productService.createProduct(dto, userId);
        em.flush(); // DB에 반영
        em.clear(); // 1차 캐시 비우기 (실제 DB 조회 테스트를 위해)

        return productDto;
    }
//------------------------------------------------------------------//
    @Test
    @Transactional
    void createProduct() {
        //given
        final String productName = "Test Product";
        final BigDecimal productPrice = BigDecimal.valueOf(12121);
        final int productStock = 12123;
        final String productDescription = "테스트 제품 설명";

        //Service 코드에서 N+1문제 확인하기 위해 영속성 컨텍스트를 비움
        em.clear();
        //when
        System.out.println("================= [로직 시작] =================");
        ProductDto product = createTestProduct(productName, productPrice, productStock, testCategoryId, testUserId, productDescription);
        System.out.println("================= [로직 종료] =================");

        //then
        //-----------------Repository로 얻은 Entity 검증
        Optional<Product> productByRepoOpt = productRepository.findById(product.id());
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
        ProductDto productDto = productService.findProduct(product.id());

        assertThat(productDto.id()).isEqualTo(productByRepo.getId());
        assertThat(productDto.createdAt()).isEqualTo(productByRepo.getCreatedAt());
        assertThat(productDto.updatedAt()).isEqualTo(productByRepo.getUpdatedAt());
        assertThat(productDto.name()).isEqualTo(productByRepo.getName());
        assertThat(productDto.price()).isEqualTo(productByRepo.getPrice());
        assertThat(productDto.stock()).isEqualTo(productByRepo.getStock());
        assertThat(productDto.description()).isEqualTo(productByRepo.getDescription());

        //Dto Seller 검증
        assertThat(productDto.seller()).isNotNull();
        SellerDto dtoSeller = productDto.seller();
        assertThat(dtoSeller.id()).isEqualTo(productByRepo.getSeller().getId());
        assertThat(dtoSeller.email()).isEqualTo(productByRepo.getSeller().getEmail());
        assertThat(dtoSeller.name()).isEqualTo(productByRepo.getSeller().getName());

        //Dto Category 검증
        assertThat(productDto.category()).isNotNull();
        CategoryDto dtoCategory = productDto.category();
        assertThat(dtoCategory.id()).isEqualTo(productByRepo.getCategory().getId());
        assertThat(dtoCategory.name()).isEqualTo(productByRepo.getCategory().getName());

        Category parent = productByRepo.getCategory().getParent();
        if (parent != null) {
            assertThat(dtoCategory.parentId()).isEqualTo(parent.getId());
            assertThat(dtoCategory.parentName()).isEqualTo(parent.getName());
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

        ProductDto product = createTestProduct(productName, productPrice, productStock, testCategoryId, testUserId, productDescription);

        em.clear();
        //when
        System.out.println("================= [로직 시작] =================");
        ProductDto productDto = productService.findProduct(product.id());
        System.out.println("================= [로직 종료] =================");

        //then
        //-----------------Service로 얻은 Dto 검증
        assertThat(productDto.id()).isEqualTo(product.id());
        assertThat(productDto.name()).isEqualTo(productName);
        assertThat(productDto.price()).isEqualByComparingTo(productPrice);
        assertThat(productDto.stock()).isEqualTo(productStock);
        assertThat(productDto.description()).isEqualTo(productDescription);

        assertThat(productDto.createdAt()).isNotNull();
        assertThat(productDto.updatedAt()).isNotNull();

        //Dto Seller 검증
        assertThat(productDto.seller()).isNotNull();
        SellerDto dtoSeller = productDto.seller();
        assertThat(dtoSeller.id()).isEqualTo(product.seller().id());
        assertThat(dtoSeller.email()).isEqualTo(product.seller().email());
        assertThat(dtoSeller.name()).isEqualTo(product.seller().name());

        //Dto Category 검증
        assertThat(productDto.category()).isNotNull();
        CategoryDto dtoCategory = productDto.category();
        assertThat(dtoCategory.id()).isEqualTo(product.category().id());
        assertThat(dtoCategory.name()).isEqualTo(product.category().name());

        assertThat(dtoCategory.parentId()).isEqualTo(productDto.category().parentId());
        assertThat(dtoCategory.parentName()).isEqualTo(productDto.category().parentName());

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
        ProductDto createdProduct = createTestProduct(originalName, originalPrice, originalStock, testCategoryId, testUserId, originalDesc);
        ProductDto product = productService.findProduct(createdProduct.id());

        final String updateName = "Updated Name";
        final BigDecimal updatePrice = BigDecimal.valueOf(33000);
        final int updateStock = 2222;
        final String updateDesc = "Update Description";

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setCategoryId(testCategoryId1);
        updateDto.setName(updateName);
        updateDto.setPrice(updatePrice);
        updateDto.setStock(updateStock);
        updateDto.setDescription(updateDesc);

        em.clear();
        //when
        System.out.println("================= [로직 시작] =================");
        ProductDto updatedDto = productService.updateProduct(updateDto, product.id(), testUserId);
        System.out.println("================= [로직 종료] =================");

        em.flush();
        em.clear();

        //then
        //DTO 검증
        assertThat(updatedDto.id()).isEqualTo(product.id());
        assertThat(updatedDto.name()).isEqualTo(updateName);
        assertThat(updatedDto.price()).isEqualByComparingTo(updatePrice);
        assertThat(updatedDto.stock()).isEqualTo(updateStock);
        assertThat(updatedDto.description()).isEqualTo(updateDesc);
        // 업데이트 시간 검증: 업데이트 이전 시간보다 이후여야 함
        assertThat(updatedDto.updatedAt()).isAfterOrEqualTo(product.updatedAt());
        // 생성 시간은 변경되지 않아야 함
        assertThat(updatedDto.createdAt()).isCloseTo(product.createdAt(), within(1, ChronoUnit.MILLIS));

        //Repository로 조회한 Entity 검증 (DB 반영 확인)
        Optional<Product> productByRepoOpt = productRepository.findById(product.id());
        assertThat(productByRepoOpt.isPresent()).isTrue();
        Product productByRepo = productByRepoOpt.get();

        assertThat(productByRepo.getName()).isEqualTo(updateName);
        assertThat(productByRepo.getPrice()).isEqualByComparingTo(updatePrice);
        assertThat(productByRepo.getStock()).isEqualTo(updateStock);
        assertThat(productByRepo.getDescription()).isEqualTo(updateDesc);

        //잘못된 id를 update할 경우
        assertThatThrownBy(()->productService.updateProduct(updateDto, 1123123L, testUserId));
    }

    @Test
    @Transactional
    void deleteById() {
        //given
        final String name = "delete Name";
        final BigDecimal price = BigDecimal.valueOf(1000);
        final int stock = 1111;
        final String description = "delete Description";
        ProductDto product = createTestProduct(name, price, stock, testCategoryId, testUserId, description);

        em.clear();
        //when
        System.out.println("================= [로직 시작] =================");
        productService.deleteById(product.id(), testUserId);
        System.out.println("================= [로직 종료] =================");

        //then
        assertThatThrownBy(()->productService.findProduct(product.id()));
    }

    @Test
    @Transactional
    void findAllProducts() {
        //given
        final String productName = "Find Test Product";
        final BigDecimal productPrice = BigDecimal.valueOf(9900);
        final int productStock = 50;
        final String productDescription = "조회 테스트 제품 설명";

        ProductDto product = createTestProduct(productName, productPrice, productStock, testCategoryId, testUserId, productDescription);
        ProductDto product1 = createTestProduct(productName + "2", productPrice, productStock + 10, testCategoryId, testUserId, productDescription + "2");

        em.clear();
        //when
        System.out.println("================= [로직 시작] =================");
        Page<ProductSummaryDto> products = productService.findProducts(PageRequest.of(0, 10));
        System.out.println("================= [로직 종료] =================");


        //then
        assertThat(products).isNotNull();
        assertThat(products.getTotalElements()).isEqualTo(2); // 전체 데이터 개수 (count 쿼리 결과)
        assertThat(products.getContent()).hasSize(2);          // 현재 페이지에 담긴 데이터 개수
        assertThat(products.getNumber()).isEqualTo(0);         // 현재 페이지 번호 (0부터 시작)
        assertThat(products.getTotalPages()).isEqualTo(1);     // 전체 페이지 수
        assertThat(products.hasNext()).isFalse();              // 다음 페이지가 있는지 여부

        //[로직 시작]과 [로직 종료] 사이에서 join Query 1번, count Query 1번 나갔는지 로그 확인
    }

    @Test
    @Transactional
    void findProductsByCategory() {
        //given
        final String productName = "Find Test Product";
        final BigDecimal productPrice = BigDecimal.valueOf(9900);
        final int productStock = 50;
        final String productDescription = "조회 테스트 제품 설명";

        ProductDto product = createTestProduct(productName, productPrice, productStock, testCategoryId, testUserId, productDescription);
        ProductDto product1 = createTestProduct(productName + "2", productPrice, productStock + 10, testCategoryId, testUserId, productDescription + "2");
        ProductDto productOtherCategory = createTestProduct(productName + "3", productPrice, productStock + 20, testCategoryId1, testUserId, productDescription + "3");

        em.clear();
        //when
        System.out.println("================= [로직1 시작] =================");
        Page<ProductSummaryDto> productsByCategory = productService.findProductsByCategory(PageRequest.of(0, 10), testCategoryId);
        System.out.println("================= [로직1 종료] =================");

        System.out.println("================= [로직2 시작] =================");
        Page<ProductSummaryDto> productsByOtherCategory = productService.findProductsByCategory(PageRequest.of(0, 10), testCategoryId1);
        System.out.println("================= [로직2 종료] =================");

        //then
        //categoryId로 조회 결과
        assertThat(productsByCategory).isNotNull();
        assertThat(productsByCategory.getTotalElements()).isEqualTo(2); // 전체 데이터 개수 (count 쿼리 결과)
        assertThat(productsByCategory.getContent()).hasSize(2);          // 현재 페이지에 담긴 데이터 개수

        //categoryId1로 조회 결과
        assertThat(productsByOtherCategory).isNotNull();
        assertThat(productsByOtherCategory.getTotalElements()).isEqualTo(1);
        assertThat(productsByOtherCategory.getContent()).hasSize(1);

        //[로직 시작]과 [로직 종료] 사이에서 Category 조회 1번,  join Query 1번, count Query 1번 나갔는지 로그 확인
    }

    @Test
    @Transactional
    void findProductsBySellerId() {
        //given
        final String productName = "Find Test Product";
        final BigDecimal productPrice = BigDecimal.valueOf(9900);
        final int productStock = 50;
        final String productDescription = "조회 테스트 제품 설명";

        ProductDto product = createTestProduct(productName, productPrice, productStock, testCategoryId, testUserId, productDescription);
        ProductDto product1 = createTestProduct(productName + "2", productPrice, productStock + 10, testCategoryId, testUserId, productDescription + "2");
        ProductDto productOtherSeller = createTestProduct(productName + "3", productPrice, productStock + 20, testCategoryId1, testUserId1, productDescription + "3");

        em.clear();
        //when
        System.out.println("================= [로직1 시작] =================");
        Page<ProductSummaryDto> productsBySeller = productService.findProductsBySellerId(PageRequest.of(0, 10), testUserId);
        System.out.println("================= [로직1 종료] =================");

        System.out.println("================= [로직2 시작] =================");
        Page<ProductSummaryDto> productsByOtherSeller = productService.findProductsBySellerId(PageRequest.of(0, 10), testUserId1);
        System.out.println("================= [로직2 종료] =================");

        //then
        assertThat(productsBySeller).isNotNull();
        assertThat(productsBySeller.getTotalElements()).isEqualTo(2); // 전체 데이터 개수 (count 쿼리 결과)
        assertThat(productsBySeller.getContent()).hasSize(2);          // 현재 페이지에 담긴 데이터 개수

        assertThat(productsByOtherSeller).isNotNull();
        assertThat(productsByOtherSeller.getTotalElements()).isEqualTo(1);
        assertThat(productsByOtherSeller.getContent()).hasSize(1);


        //[로직 시작]과 [로직 종료] 사이에서 join Query 1번, count Query 1번 나갔는지 로그 확인
    }
}