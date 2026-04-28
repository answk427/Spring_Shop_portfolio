package work.trade.order.mapper;

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

    User getUser(String email, String password, String name) {
        return new User(email, password, null, name, Role.USER);
    }

    Product getProduct(User seller, String name, String desc, BigDecimal price, Integer stock) {
        Category category = Category.builder().parent(null).name("category").build();
        return Product.builder()
                .seller(seller)
                .category(category)
                .name(name)
                .description(desc)
                .price(price)
                .stock(stock)
                .build();
    }

    Order getOrder() {
        User buyer = getUser("buyer@naver.com", "asdf1234", "buyer");
        User seller = getUser("seller@naver.com", "asdf1234", "seller");

        OrderItem orderItem1 = OrderItem.builder()
                .product(getProduct(seller, "product1", "Desc", BigDecimal.valueOf(1111), 100))
                .quantity(13)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .product(getProduct(seller, "product2", "Desc", BigDecimal.valueOf(2222), 1000))
                .quantity(33)
                .build();

        OrderStatus orderStatus = OrderStatus.builder()
                .code("PENDING")
                .name("pending")
                .description("pending상태")
                .build();

        return Order.builder()
                .buyer(buyer)
                .orderItems(List.of(orderItem1, orderItem2))
                .status(orderStatus)
                .build();
    }

    void checkOrderItem(OrderItemDto orderItemDto, OrderItem orderItem) {
        assertThat(orderItemDto.getId()).isEqualTo(orderItem.getId());
        assertThat(orderItemDto.getQuantity()).isEqualTo(orderItem.getQuantity());
        assertThat(orderItemDto.getUnitPrice()).isEqualTo(orderItem.getUnitPrice());
        assertThat(orderItemDto.getSubtotalPrice()).isEqualTo(orderItem.getSubtotalPrice());
    }

    void checkProduct(ProductSummaryDto productDto, Product product) {
        assertThat(productDto.id()).isEqualTo(product.getId());
        assertThat(productDto.stock()).isEqualTo(product.getStock());
        assertThat(productDto.name()).isEqualTo(product.getName());
        assertThat(productDto.price()).isEqualTo(product.getPrice());
        assertThat(productDto.sellerName()).isEqualTo(product.getSeller().getName());
    }

    void checkStatus(OrderStatus status, OrderStatusDto statusDto) {
        assertThat(status.getName()).isEqualTo(statusDto.name());
        assertThat(status.getCode()).isEqualTo(statusDto.code());
        assertThat(status.getDescription()).isEqualTo(statusDto.description());
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
        assertThat(orderDto.id()).isEqualTo(order.getId());
        assertThat(orderDto.buyerId()).isEqualTo(order.getBuyer().getId());
        assertThat(orderDto.totalPrice()).isEqualTo(order.getTotalPrice());

        //orderItem 검사
        for (int i = 0; i < orderItems.size(); ++i) {
            OrderItemDto orderItemDto = orderDto.orderItems().get(i);
            OrderItem orderItem = orderItems.get(i);
            checkOrderItem(orderItemDto, orderItem);

            ProductSummaryDto productDto = orderItemDto.getProduct();
            Product product = orderItem.getProduct();
            checkProduct(productDto, product);
        }

        //Status 검사
        checkStatus(order.getStatus(), orderDto.status());
    }

    @Test
    void toOrderSummaryDto() {
        //given
        Order order = getOrder();

        //when
        OrderSummaryDto orderSummaryDto = orderMapper.toOrderSummaryDto(order);

        //then
        assertThat(orderSummaryDto.id()).isEqualTo(order.getId());
        assertThat(orderSummaryDto.totalPrice()).isEqualTo(order.getTotalPrice());
        assertThat(orderSummaryDto.itemCount()).isEqualTo(order.getOrderItems().size());

        checkStatus(order.getStatus(), orderSummaryDto.status());
        assertThat(orderSummaryDto.itemCount()).isEqualTo(order.getOrderItems().size());
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