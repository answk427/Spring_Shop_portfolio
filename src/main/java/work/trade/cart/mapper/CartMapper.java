package work.trade.cart.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import work.trade.cart.domain.Cart;
import work.trade.cart.dto.request.CartAddRequestDto;
import work.trade.cart.dto.request.CartUpdateRequestDto;
import work.trade.cart.dto.response.CartDto;
import work.trade.product.domain.Product;
import work.trade.product.mapper.ProductMapper;
import work.trade.user.domain.User;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartMapper {
//Request -> Entity
//-------------------------------------//
    @Mapping(target = "quantity", source = "dto.quantity")
    @Mapping(target = "user", source = "user") // 두 번째 인자 User 객체 통째로
    @Mapping(target = "product", source = "product") // 세 번째 인자 Product 객체 통째로
    Cart toEntity(CartAddRequestDto dto, User user, Product product);

//Entity -> Response
//-------------------------------------//
    @Mapping(target = "product", source = "cart.product")
    CartDto toDto(Cart cart);
}
