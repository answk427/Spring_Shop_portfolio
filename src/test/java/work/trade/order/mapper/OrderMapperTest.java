package work.trade.order.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import work.trade.auth.role.Role;
import work.trade.order.domain.Order;
import work.trade.order.domain.OrderItem;
import work.trade.order.domain.OrderStatus;
import work.trade.order.dto.response.order.OrderDto;
 import work.trade.order.dto.response.order.OrderStatusDto;
import work.trade.order.dto.response.order.OrderSummaryDto;
import work.trade.order.dto.response.orderItem.OrderItemDto;
import work.trade.order.repository.OrderStatusRepository;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.user.domain.User;
import work.trade.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

//*********************************//
    Order getOrder() {
        User buyer = User.builder()
                .name("Buyer2")
                .email("Buyer2@naver.com")
                .passwordHash("PWHASH")
                .role(Role.USER)
                .build();

        User seller = User.builder()
                .name("Seller2")
                .email("Seller2@naver.com")
                .passwordHash("PWHASH2")
                .role(Role.USER)
                .build();

        User savedBuyer = userRepository.save(buyer);
        User savedSeller = userRepository.save(seller);

        Category category = Category.builder()
                .name("category")
                .build();

        Product product1 = Product.builder()
                .seller(savedSeller)
                .category(category)
                .stock(100)
                .name("product1")
                .price(new BigDecimal(1111))
                .description("Desc")
                .build();

        Product product2 = Product.builder()
                .seller(savedSeller)
                .category(category)
                .stock(1000)
                .name("product2")
                .price(new BigDecimal(2222))
                .description("Desc")
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .product(product1)
                .quantity(13)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .product(product2)
                .quantity(33)
                .build();

        OrderStatus orderStatus = OrderStatus.builder()
                .code("PENDING")
                .name("pending")
                .description("pending상태")
                .build();

        Order order = Order.builder()
                .buyer(savedBuyer)
                .orderItems(List.of(orderItem1, orderItem2))
                .status(orderStatus)
                .build();

        return order;
    }

    void checkOrderItem(OrderItemDto orderItemDto, OrderItem orderItem) {
        assertThat(orderItemDto.getId()).isEqualTo(orderItem.getId());
        assertThat(orderItemDto.getQuantity()).isEqualTo(orderItem.getQuantity());
        assertThat(orderItemDto.getUnitPrice()).isEqualTo(orderItem.getUnitPrice());
        assertThat(orderItemDto.getSubtotalPrice()).isEqualTo(orderItem.getSubtotalPrice());
    }

    void checkProduct(ProductSummaryDto productDto, Product product) {
        assertThat(productDto.getId()).isEqualTo(product.getId());
        assertThat(productDto.getStock()).isEqualTo(product.getStock());
        assertThat(productDto.getName()).isEqualTo(product.getName());
        assertThat(productDto.getPrice()).isEqualTo(product.getPrice());
        assertThat(productDto.getSellerName()).isEqualTo(product.getSeller().getName());
    }

    void checkStatus(OrderStatus status, OrderStatusDto statusDto) {
        assertThat(status.getName()).isEqualTo(statusDto.getName());
        assertThat(status.getCode()).isEqualTo(statusDto.getCode());
        assertThat(status.getDescription()).isEqualTo(statusDto.getDescription());
    }

//*********************************//

    @Test
    void orderCreate() {
        //given
        Order order = getOrder();
        List<OrderItem> orderItems = order.getOrderItems();
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            totalPrice = totalPrice.add(item.getSubtotalPrice());
        }

        //when, then
        assertThat(totalPrice).isEqualTo(order.getTotalPrice());

        //양방향 확인
        for (OrderItem item : orderItems) {
            assertThat(item.getOrder()).isSameAs(order);
        }
    }
    @Test
    void toOrderDto() {
        //given
        Order order = getOrder();
        List<OrderItem> orderItems = order.getOrderItems();
        //when
        OrderDto orderDto = orderMapper.toOrderDto(order);

        //then
        assertThat(orderDto.getId()).isEqualTo(order.getId());
        assertThat(orderDto.getBuyerId()).isEqualTo(order.getBuyer().getId());
        assertThat(orderDto.getTotalPrice()).isEqualTo(order.getTotalPrice());

        //orderItem 검사
        for (int i = 0; i < orderItems.size(); ++i) {
            OrderItemDto orderItemDto = orderDto.getOrderItems().get(i);
            OrderItem orderItem = orderItems.get(i);
            checkOrderItem(orderItemDto, orderItem);

            ProductSummaryDto productDto = orderItemDto.getProduct();
            Product product = orderItem.getProduct();
            checkProduct(productDto, product);
        }

        //Status 검사
        checkStatus(order.getStatus(), orderDto.getStatus());
    }

    @Test
    void toOrderSummaryDto() {
        //given
        Order order = getOrder();

        //when
        OrderSummaryDto orderSummaryDto = orderMapper.toOrderSummaryDto(order);

        //then
        assertThat(orderSummaryDto.getId()).isEqualTo(order.getId());
        assertThat(orderSummaryDto.getTotalPrice()).isEqualTo(order.getTotalPrice());
        assertThat(orderSummaryDto.getItemCount()).isEqualTo(order.getOrderItems().size());

        checkStatus(order.getStatus(), orderSummaryDto.getStatus());
        OrderStatusDto statusDto = orderSummaryDto.getStatus();
        OrderStatus status = order.getStatus();
        assertThat(orderSummaryDto.getItemCount()).isEqualTo(order.getOrderItems().size());
    }

    @Test
    void toOrderItemDto() {
        //given
        Order order = getOrder();
        List<OrderItem> orderItems = order.getOrderItems();

        //when
        OrderItemDto orderItemDto1 = orderMapper.toOrderItemDto(orderItems.get(0));
        OrderItemDto orderItemDto2 = orderMapper.toOrderItemDto(orderItems.get(1));
        List<OrderItemDto> orderItemDtos = List.of(orderItemDto1, orderItemDto2);

        //then
        for (int i = 0; i < orderItems.size(); ++i) {
            checkOrderItem(orderItemDtos.get(i), orderItems.get(i));
            checkProduct(orderItemDtos.get(i).getProduct(), orderItems.get(i).getProduct());
        }
    }
}