package work.trade.order.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.cart.exception.CartEmptyException;
import work.trade.cart.repository.CartRepository;
import work.trade.cart.service.CartService;
import work.trade.order.domain.OrderStatus;
import work.trade.order.domain.constant.OrderStatusConstant;
import work.trade.order.dto.response.order.OrderDto;
import work.trade.order.dto.response.order.OrderSummaryDto;
import work.trade.order.dto.response.orderItem.OrderItemDto;
import work.trade.order.repository.OrderItemRepository;
import work.trade.order.repository.OrderRepository;
import work.trade.order.repository.OrderStatusRepository;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.product.service.ProductService;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.repository.UserRepository;
import work.trade.user.service.UserService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Testcontainers
class OrderServiceTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryService;

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

//*********************************//
    private Long buyerId;
    private Long sellerId;
    private Long productId1;
    private Long productId2;

    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리 (참조 무결성 역순으로 삭제)
        cartRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void init() {

        //User
        UserDto buyerDto = createUser("Buyer", "buyer@naver.com", "asdf1234");
        UserDto sellerDto = createUser("Seller", "seller@naver.com", "asdf1234");
        buyerId = buyerDto.getId();
        sellerId = sellerDto.getId();

        //Category
        Category category = categoryService.findById(1L).get();

        //Product
        ProductCreateRequestDto productCreateRequestDto1 = new ProductCreateRequestDto();
        productCreateRequestDto1.setCategoryId(category.getId());
        productCreateRequestDto1.setStock(100);
        productCreateRequestDto1.setPrice(new BigDecimal(1111));
        productCreateRequestDto1.setDescription("Product1 DESC");
        productCreateRequestDto1.setName("Product1");

        ProductCreateRequestDto productCreateRequestDto2 = new ProductCreateRequestDto();
        productCreateRequestDto2.setCategoryId(category.getId());
        productCreateRequestDto2.setStock(200);
        productCreateRequestDto2.setPrice(new BigDecimal(2222));
        productCreateRequestDto2.setDescription("Product2 DESC");
        productCreateRequestDto2.setName("Product2");

        ProductDto product1 = productService.createProduct(productCreateRequestDto1, sellerDto.getId());
        ProductDto product2 = productService.createProduct(productCreateRequestDto2, sellerDto.getId());
        productId1 = product1.getId();
        productId2 = product2.getId();
    }

    @Transactional
    UserDto createUser(String name, String email, String password) {
        UserCreateRequestDto createUserRequestDto = new UserCreateRequestDto();
        createUserRequestDto.setName(name);
        createUserRequestDto.setEmail(email);
        createUserRequestDto.setPassword(password);

        return userService.createUser(createUserRequestDto);
    }

    @Transactional
    void addToCart(Long userId, Long productId, Integer quantity) {
        CartAddRequestDto cartAddRequestDto = new CartAddRequestDto();
        cartAddRequestDto.setProductId(productId);
        cartAddRequestDto.setQuantity(quantity);
        cartService.addToCart(cartAddRequestDto, userId);
    }

    void checkProduct(ProductSummaryDto productDto, ProductDto product) {
        assertThat(productDto.getId()).isEqualTo(product.getId());
        assertThat(productDto.getStock()).isEqualTo(product.getStock());
        assertThat(productDto.getName()).isEqualTo(product.getName());
        assertThat(productDto.getPrice()).isEqualTo(product.getPrice());
        assertThat(productDto.getSellerName()).isEqualTo(product.getSeller().getName());
    }

    void checkProduct(ProductSummaryDto productDto1, ProductSummaryDto productDto2) {
        assertThat(productDto1.getId()).isEqualTo(productDto2.getId());
        assertThat(productDto1.getStock()).isEqualTo(productDto2.getStock());
        assertThat(productDto1.getName()).isEqualTo(productDto2.getName());
        assertThat(productDto1.getPrice()).isEqualTo(productDto2.getPrice());
        assertThat(productDto1.getSellerName()).isEqualTo(productDto2.getSellerName());
    }

    void checkStatus(OrderStatus status1, OrderStatus status2) {
        assertThat(status1.getName()).isEqualTo(status2.getName());
        assertThat(status1.getCode()).isEqualTo(status2.getCode());
        assertThat(status1.getDescription()).isEqualTo(status2.getDescription());
    }

    void checkOrder(OrderDto orderDto1, OrderDto orderDto2) {
        assertThat(orderDto1.getId()).isEqualTo(orderDto2.getId());
        assertThat(orderDto1.getBuyerId()).isEqualTo(orderDto2.getBuyerId());
        assertThat(orderDto1.getTotalPrice()).isEqualTo(orderDto2.getTotalPrice());

        List<OrderItemDto> orderItems1 = orderDto1.getOrderItems();
        List<OrderItemDto> orderItems2 = orderDto2.getOrderItems();
        for (int i = 0; i < orderItems1.size(); ++i) {
            OrderItemDto orderItemDto1 = orderItems1.get(i);
            OrderItemDto orderItemDto2 = orderItems2.get(i);

            assertThat(orderItemDto1.getId()).isEqualTo(orderItemDto2.getId());
            assertThat(orderItemDto1.getSubtotalPrice()).isEqualTo(orderItemDto2.getSubtotalPrice());
            assertThat(orderItemDto1.getUnitPrice()).isEqualTo(orderItemDto2.getUnitPrice());
            assertThat(orderItemDto1.getQuantity()).isEqualTo(orderItemDto2.getQuantity());

            checkProduct(orderItemDto1.getProduct(), orderItemDto2.getProduct());
        }

        checkStatus(orderDto1.getStatus(), orderDto2.getStatus());
    }

//*********************************//

    @Test
    void checkEngine() {
        // 1. 현재 접속한 DB의 모든 테이블과 엔진을 다 출력해버리자 (디버깅용)
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE()"
        );

        log.info("@@@ [TABLE LIST START]");
        tables.forEach(table ->
                log.info("Table: {}, Engine: {}", table.get("TABLE_NAME"), table.get("ENGINE"))
        );
        log.info("@@@ [TABLE LIST END]");

        // 2. 특정해서 가져오기 (LIKE 사용해서 대소문자 회피)
        String engine = jdbcTemplate.queryForObject(
                "SELECT ENGINE FROM information_schema.TABLES " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'products' LIMIT 1",
                String.class
        );
        log.info("@@@ [RESULT ENGINE]: {}", engine);
    }

    @Test
    @Transactional
    void createOrderFromCart() {
        //given
        //장바구니 생성
        List<Integer> quantities = List.of(10, 20);
        addToCart(buyerId, productId1, quantities.get(0));
        addToCart(buyerId, productId2, quantities.get(1));

        //주문 생성 전 product
        ProductDto oldProduct1 = productService.findProduct(productId1);
        ProductDto oldProduct2 = productService.findProduct(productId2);
        List<ProductDto> oldProducts = List.of(oldProduct1, oldProduct2);

        //주문 총금액 계산 전
        BigDecimal totalSum = BigDecimal.ZERO;

        //when
        OrderDto orderDto = orderService.createOrderFromCart(buyerId);
        List<OrderItemDto> orderItems = orderDto.getOrderItems();

        //주문 생성 이후 product
        ProductDto product1 = productService.findProduct(oldProduct1.getId());
        ProductDto product2 = productService.findProduct(oldProduct2.getId());
        List<ProductDto> products = List.of(product1, product2);

        //then
        //orderItem 검증
        for (int i = 0; i < orderItems.size(); ++i) {
            OrderItemDto orderItemDto = orderItems.get(i);
            ProductDto oldProductDto = oldProducts.get(i);
            ProductDto productDto = products.get(i);

            //줄어든 재고 검증
            assertThat(productDto.getStock()).isEqualTo(oldProductDto.getStock() - orderItemDto.getQuantity());

            //주문 상품 금액 검증
            BigDecimal sum = oldProductDto.getPrice().multiply(new BigDecimal(quantities.get(i)));
            assertThat(orderItemDto.getSubtotalPrice()).isEqualTo(sum);
            totalSum = totalSum.add(sum);

            assertThat(orderItemDto.getId()).isNotNull();
            assertThat(orderItemDto.getQuantity()).isEqualTo(quantities.get(i));
            assertThat(orderItemDto.getUnitPrice()).isEqualTo(oldProductDto.getPrice());

            checkProduct(orderItemDto.getProduct(), productDto);
        }

        assertThat(orderDto.getId()).isNotNull();
        assertThat(orderDto.getBuyerId()).isEqualTo(buyerId);
        assertThat(orderDto.getTotalPrice()).isEqualTo(totalSum);

        OrderStatus status = orderStatusRepository.findById(OrderStatusConstant.PENDING).get();
        checkStatus(orderDto.getStatus(), status);
        
        //장바구니가 비어있어야함
        List<CartDto> myCart = cartService.getMyCart(buyerId);
        assertThat(myCart).isEmpty();

        //장바구니가 비어있는 상태로 오더를 만들었을 때
        assertThatThrownBy(() -> orderService.createOrderFromCart(buyerId)).isInstanceOf(CartEmptyException.class);
    }

    @Test
    @Transactional
    void getOrder() {
        //given
        addToCart(buyerId, productId1, 10);
        OrderDto oldOrderDto = orderService.createOrderFromCart(buyerId);

        //when
        OrderDto orderDto = orderService.getOrder(oldOrderDto.getId(), buyerId);

        //then
        checkOrder(oldOrderDto, orderDto);
    }

    @Test
    @Transactional
    void getUserOrders() throws InterruptedException {
        //given
        addToCart(buyerId, productId1, 10);
        OrderDto order1 = orderService.createOrderFromCart(buyerId);

        Thread.sleep(1000);

        addToCart(buyerId, productId2, 20);
        OrderDto order2 = orderService.createOrderFromCart(buyerId);

        //when
        Page<OrderSummaryDto> result =
                orderService.getUserOrders(buyerId, Pageable.unpaged());

        List<OrderSummaryDto> content = result.getContent();

        //then
        //1. 개수 검증
        assertThat(content).hasSize(2);

        //2. 최신순 정렬 검증 (order2가 먼저 와야됨)
        OrderSummaryDto first = content.get(0);
        OrderSummaryDto second = content.get(1);

        assertThat(first.getId()).isEqualTo(order2.getId());
        assertThat(second.getId()).isEqualTo(order1.getId());

        //3. 내용 검증
        assertThat(first.getItemCount()).isEqualTo(order2.getOrderItems().size());
        assertThat(second.getItemCount()).isEqualTo(order2.getOrderItems().size());

        assertThat(first.getTotalPrice()).isEqualTo(order2.getTotalPrice());
        assertThat(second.getTotalPrice()).isEqualTo(order1.getTotalPrice());

        // 4. 상태 검증 (둘 다 PENDING)
        OrderStatus pending = orderStatusRepository.findById(OrderStatusConstant.PENDING).get();

        checkStatus(first.getStatus(), pending);
        checkStatus(second.getStatus(), pending);
    }

    @Test
    @Transactional
    void getUserOrdersEmpty() {
        Page<OrderSummaryDto> result =
                orderService.getUserOrders(buyerId, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @Transactional
    void getUserOrdersByStatus() {
        //given
        //주문 2개 생성 (둘 다 PENDING)
        addToCart(buyerId, productId1, 10);
        OrderDto order1 = orderService.createOrderFromCart(buyerId);

        addToCart(buyerId, productId2, 20);
        OrderDto order2 = orderService.createOrderFromCart(buyerId);

        //하나 상태 변경 (CONFIRMED)
        orderService.executeByStatus(order1.getId(), buyerId, OrderStatusConstant.CONFIRMED);

        //조회할 상태
        OrderStatus confirmedStatus = orderStatusRepository.findById(OrderStatusConstant.CONFIRMED).get();

        //when
        Page<OrderSummaryDto> result =
                orderService.getUserOrdersByStatus(buyerId, confirmedStatus, Pageable.unpaged());

        List<OrderSummaryDto> content = result.getContent();

        //then
        //1. 개수 (1개만 나와야 함)
        assertThat(content).hasSize(1);
        OrderSummaryDto dto = content.get(0);

        //2. order1만 조회되어야 함
        assertThat(dto.getId()).isEqualTo(order1.getId());

        //3. 상태 검증
        checkStatus(dto.getStatus(), confirmedStatus);

        //4. 기본 정보 검증
        assertThat(dto.getTotalPrice()).isEqualTo(order1.getTotalPrice());
    }

    @Test
    @Transactional
    void getUserOrdersByStatusEmpty() {
        //given
        OrderStatus shippedStatus = orderStatusRepository
                .findById(OrderStatusConstant.SHIPPED)
                .orElseThrow();

        //when
        Page<OrderSummaryDto> result =
                orderService.getUserOrdersByStatus(buyerId, shippedStatus, Pageable.unpaged());

        //then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @Transactional
    void executeByStatusSuccessFlow() {
        //given
        addToCart(buyerId, productId1, 10);
        OrderDto order = orderService.createOrderFromCart(buyerId);

        //when
        OrderDto confirmed = orderService.executeByStatus(
                order.getId(), buyerId, OrderStatusConstant.CONFIRMED);

        OrderDto shipped = orderService.executeByStatus(
                order.getId(), buyerId, OrderStatusConstant.SHIPPED);

        OrderDto delivered = orderService.executeByStatus(
                order.getId(), buyerId, OrderStatusConstant.DELIVERED);

        //then
        OrderStatus confirmedStatus = orderStatusRepository.findById(OrderStatusConstant.CONFIRMED).orElseThrow();
        OrderStatus shippedStatus = orderStatusRepository.findById(OrderStatusConstant.SHIPPED).orElseThrow();
        OrderStatus deliveredStatus = orderStatusRepository.findById(OrderStatusConstant.DELIVERED).orElseThrow();

        checkStatus(confirmed.getStatus(), confirmedStatus);
        checkStatus(shipped.getStatus(), shippedStatus);
        checkStatus(delivered.getStatus(), deliveredStatus);
    }

    @Test
    @DisplayName("동시에 100명이 재고가 10개인 상품을 주문하면, 10명만 성공해야 한다")
    void stockConcurrencyTest() throws InterruptedException {
        // Given: 상품 재고 10개 설정 및 사용자 생성
        ProductCreateRequestDto productCreateRequestDto = new ProductCreateRequestDto();
        productCreateRequestDto.setCategoryId(1L);
        productCreateRequestDto.setStock(10);
        productCreateRequestDto.setPrice(new BigDecimal(1000));
        productCreateRequestDto.setDescription("concurrencyProductDesc");
        productCreateRequestDto.setName("concurrencyProductName");

        Long productId = productService.createProduct(productCreateRequestDto, sellerId).getId();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // When: 100명이 동시에 주문 시도
        for (int i = 0; i < threadCount; i++) {
            Long userId = createUser("user" + i, "email" + i + "@naver.com", "password1234" + i).getId();
            addToCart(userId, productId, 1); // 1개씩 담기

            executorService.submit(() -> {
                try {
                    //        barrier.await(); // 모든 스레드가 준비될 때까지 대기
                    log.info("[{}] 주문 시작", Thread.currentThread().getName());
                    orderService.createOrderFromCart(userId);
                    successCount.incrementAndGet();
                    log.info("[{}] 주문 성공", Thread.currentThread().getName());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.warn("[{}] 주문 실패 - {}: {}", Thread.currentThread().getName(), e.getClass().getSimpleName(), e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 완료될 때까지 대기
        boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 성공 10건, 실패 90건, 남은 재고 0개 확인
        ProductDto product = productService.findProduct(productId);

        log.info("=== 테스트 결과 ===");
        log.info("성공 주문 수: {}", successCount.get());
        log.info("실패 주문 수: {}", failCount.get());
        log.info("최종 재고: {}", product.getStock());
        log.info("모든 스레드 완료: {}", completed);

        assertThat(successCount.get())
                .as("정확히 10명만 주문에 성공해야 함")
                .isEqualTo(10);

        assertThat(failCount.get())
                .as("90명이 재고 부족으로 실패해야 함")
                .isEqualTo(90);

        assertThat(product.getStock())
                .as("최종 재고는 0이어야 함")
                .isEqualTo(0);
    }


    @Test
    @DisplayName("비관적 락이 실제로 다른 스레드를 대기시키는지 확인")
    void pessimisticLockWaitTest() throws InterruptedException {
        // 테스트 실행 전에 TransactionTemplate 초기화
        transactionTemplate = new TransactionTemplate(transactionManager);


        // 1. 테스트용 상품 생성 및 저장 (트랜잭션 분리를 위해 별도 저장)
        Optional<Category> category = categoryService.findById(1L);
        Optional<User> seller = userRepository.findById(sellerId);
        Product product = Product.builder()
                .name("Lock Test")
                .stock(100)
                .category(category.get())
                .seller(seller.get())
                .price(BigDecimal.TEN)
                .build();

        productRepository.saveAndFlush(product);
        Long productId = product.getId();

        CountDownLatch lockAcquiredLatch = new CountDownLatch(1); // 락 획득 신호
        CountDownLatch releaseLockLatch = new CountDownLatch(1);  // 락 해제 신호

        // 스레드 A: 락을 획득하고 유지함
        Thread threadA = new Thread(() -> {
            // 별도 트랜잭션을 시작하기 위해 직접 구현하거나,
            // 외부 클래스의 @Transactional 메서드를 호출해야 함
            transactionTemplate.execute(status -> {
                log.info("스레드 A: 락 획득 시도");
                Product p = productRepository.findByIdWithLock(productId).orElseThrow();
                log.info("스레드 A: 락 획득 성공");

                lockAcquiredLatch.countDown(); // 락 잡았다고 알림
                try {
                    releaseLockLatch.await(); // 메인 스레드에서 신호 줄 때까지 락 안 놓음
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("스레드 A: 트랜잭션 종료(락 해제)");
                return null;
            });
        });

        // 스레드 B: 스레드 A가 락을 잡고 있을 때 접근 시도
        Thread threadB = new Thread(() -> {
            try {
                lockAcquiredLatch.await(); // 스레드 A가 먼저 락을 잡을 때까지 대기
                log.info("스레드 B: 락 획득 시도 (대기 예상)");

                transactionTemplate.execute(status -> {
                    Product p = productRepository.findByIdWithLock(productId).orElseThrow();
                    log.info("스레드 B: 락 획득 성공!");
                    return null;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        threadA.start();
        threadB.start();

        // 스레드 B가 락을 얻으려고 시도하는 시간을 확보
        Thread.sleep(2000);

        log.info("메인 스레드: 이제 락을 풀어줍니다.");
        releaseLockLatch.countDown(); // 스레드 A 종료 유도

        threadA.join();
        threadB.join();
    }
}
