package work.trade.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import work.trade.order.domain.constant.OrderStatusConstant;
import work.trade.order.dto.request.OrderStatusUpdateRequestDto;
import work.trade.order.dto.response.order.OrderDto;
import work.trade.order.dto.response.order.OrderStatusDto;
import work.trade.order.dto.response.order.OrderSummaryDto;
import work.trade.order.dto.response.orderItem.OrderItemDto;
import work.trade.order.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;


//*******************************//

    OrderDto createOrderDto(OrderStatusDto statusDto) {
        OrderDto orderDto = new OrderDto(10L,
                1L,
                statusDto,
                new BigDecimal(10000),
                List.of(new OrderItemDto()),
                null,
                null);

        return orderDto;
    }

//**************************************//

    @Test
    @DisplayName("주문 생성 - POST /api/orders")
    @WithMockUser(username = "1") // Authentication.getName()이 "1"을 반환하도록 설정
    void createOrder() throws Exception {
        // given
        OrderDto responseDto = createOrderDto(new OrderStatusDto(OrderStatusConstant.PENDING, "name", "desc"));

        when(orderService.createOrderFromCart(anyLong())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.totalPrice").value(10000))
                .andExpect(jsonPath("$.orderItems").isArray());
    }


    @Test
    @DisplayName("주문 상세 조회 - GET /api/orders/{orderId}")
    @WithMockUser(username = "1")
    void getOrder() throws Exception {
        // given
        Long orderId = 10L;
        OrderDto responseDto = createOrderDto(new OrderStatusDto(OrderStatusConstant.PENDING, "name", "desc"));
        when(orderService.getOrder(eq(orderId), anyLong())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.totalPrice").value(10000))
                .andExpect(jsonPath("$.orderItems").isArray());
    }

    @Test
    @DisplayName("상태별 주문 목록 조회 - GET /api/orders/status/{status}")
    @WithMockUser(username = "1")
    void getUserOrdersByStatus() throws Exception {
        // given
        String statusCode = OrderStatusConstant.PENDING;
        OrderStatusDto orderStatusDto = new OrderStatusDto(statusCode, "name", "desc");

        OrderSummaryDto summary = new OrderSummaryDto(10L, orderStatusDto, new BigDecimal(10000), 10, null);

        Page<OrderSummaryDto> pageResponse = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);
        when(orderService.getUserOrdersByStatus(anyLong(), eq(statusCode), any())).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/orders/status/{status}", statusCode)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.content[0].status.code").value(statusCode))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("주문 상태 변경 - PATCH /api/orders/{orderId}/status")
    @WithMockUser(username = "1")
    void setOrderStatus() throws Exception {
        // given
        Long orderId = 10L;
        String targetStatus = OrderStatusConstant.CONFIRMED;
        OrderStatusUpdateRequestDto requestDto = new OrderStatusUpdateRequestDto(targetStatus);

        OrderStatusDto orderStatusDto = new OrderStatusDto(targetStatus, "name", "desc");
        OrderDto responseDto = createOrderDto(orderStatusDto);

        when(orderService.executeByStatus(eq(orderId), anyLong(), eq(targetStatus)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/orders/{orderId}/status", orderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(targetStatus));
    }

    @Test
    @DisplayName("모든 주문 목록 조회 - GET /api/orders")
    @WithMockUser(username = "1")
    void getOrders() throws Exception {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        OrderSummaryDto order1 = new OrderSummaryDto(
                1L,
                new OrderStatusDto("PENDING", "name", "desc"),
                new BigDecimal("10000"),
                10,
                null
        );
        OrderSummaryDto order2 = new OrderSummaryDto(
                2L,
                new OrderStatusDto("CONFIRMED", "name", "desc"),
                new BigDecimal("20000"),
                20,
                null
        );

        Page<OrderSummaryDto> orderPage = new PageImpl<>(
                List.of(order1, order2),
                pageable,
                2
        );

        given(orderService.getUserOrders(userId, pageable))
                .willReturn(orderPage);

        // when
        mockMvc.perform(
                        get("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "10")
                )
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].totalPrice").value(10000))
                .andExpect(jsonPath("$.content[0].status.code").value("PENDING"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].totalPrice").value(20000))
                .andExpect(jsonPath("$.content[1].status.code").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

}