package work.trade.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.user.domain.User;
import work.trade.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface ProductMapper {

//Request -> Entity
//-------------------------------------//
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "price", source = "dto.price")
    @Mapping(target = "stock", source = "dto.stock")
    @Mapping(target = "seller", source = "seller") // 두 번째 인자 User 객체 통째로
    @Mapping(target = "category", source = "category") // 세 번째 인자 Category 객체 통째로
    Product toEntity(ProductCreateRequestDto dto, User seller, Category category);

//Entity -> Response
//-------------------------------------//
    ProductDto toDto(Product product);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "sellerName", source = "seller.name")
    ProductSummaryDto toSummaryDto(Product product);
}
