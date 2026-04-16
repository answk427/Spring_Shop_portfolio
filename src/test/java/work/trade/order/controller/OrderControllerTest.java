package work.trade.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

import static org.mockito.ArgumentMatchers.*;
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

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;


    //**************************************//
    OrderDto createOrderDto(OrderStatusDto statusDto) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(10L);
        orderDto.setTotalPrice(new BigDecimal(10000));
        orderDto.setBuyerId(1L);
        orderDto.setStatus(statusDto);
        orderDto.setOrderItems(List.of(new OrderItemDto()));

        return orderDto;
    }

//**************************************//

    @Test
    @DisplayName("주문 생성 - POST /orders")
    @WithMockUser(username = "1") // Authentication.getName()이 "1"을 반환하도록 설정
    void createOrder() throws Exception {
        // given
        OrderDto responseDto = createOrderDto(new OrderStatusDto());

        when(orderService.createOrderFromCart(anyLong())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.totalPrice").value(10000))
                .andExpect(jsonPath("$.orderItems").isArray());
    }


    @Test
    @DisplayName("주문 상세 조회 - GET /orders/{orderId}")
    @WithMockUser(username = "1")
    void getOrder() throws Exception {
        // given
        Long orderId = 10L;
        OrderDto responseDto = createOrderDto(new OrderStatusDto());
        when(orderService.getOrder(eq(orderId), anyLong())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.totalPrice").value(10000))
                .andExpect(jsonPath("$.orderItems").isArray());
    }

    @Test
    @DisplayName("상태별 주문 목록 조회 - GET /orders/status/{status}")
    @WithMockUser(username = "1")
    void getUserOrdersByStatus() throws Exception {
        // given
        String statusCode = OrderStatusConstant.PENDING;
        OrderStatusDto orderStatusDto = new OrderStatusDto();
        orderStatusDto.setCode(statusCode);

        OrderSummaryDto summary = new OrderSummaryDto();
        summary.setId(10L);
        summary.setStatus(orderStatusDto);
        summary.setTotalPrice(new BigDecimal(10000));
        summary.setItemCount(10);

        Page<OrderSummaryDto> pageResponse = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);
        when(orderService.getUserOrdersByStatus(anyLong(), eq(statusCode), any())).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/orders/status/{status}", statusCode)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.content[0].status.code").value(statusCode))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("주문 상태 변경 - PATCH /orders/{orderId}/status")
    @WithMockUser(username = "1")
    void setOrderStatus() throws Exception {
        // given
        Long orderId = 10L;
        String targetStatus = OrderStatusConstant.CONFIRMED;
        OrderStatusUpdateRequestDto requestDto = new OrderStatusUpdateRequestDto();
        requestDto.setStatus(targetStatus);

        OrderStatusDto orderStatusDto = new OrderStatusDto();
        orderStatusDto.setCode(targetStatus);
        OrderDto responseDto = createOrderDto(orderStatusDto);

        when(orderService.executeByStatus(eq(orderId), anyLong(), eq(targetStatus)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(targetStatus));
    }
}

