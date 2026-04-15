package work.trade.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import work.trade.order.domain.Order;
import work.trade.order.domain.OrderItem;
import work.trade.order.dto.response.order.OrderDto;
import work.trade.order.dto.response.order.OrderSummaryDto;
import work.trade.order.dto.response.orderItem.OrderItemDto;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.product.mapper.CategoryMapper;
import work.trade.product.mapper.ProductMapper;
import work.trade.user.domain.User;
import work.trade.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface OrderMapper {

//Entity -> Response
//-------------------------------------//
    @Mapping(target = "buyerId", source = "order.buyer.id")
    OrderDto toOrderDto(Order order);

    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    OrderSummaryDto toOrderSummaryDto(Order order);

    OrderItemDto toOrderItemDto(OrderItem orderItem);

}

