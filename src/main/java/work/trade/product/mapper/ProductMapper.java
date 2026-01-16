package work.trade.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface ProductMapper {

//Request -> Entity
//-------------------------------------//

    //User, Category는 Service에서 처리
    Product toEntity(ProductCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    //Category는 Service에서 처리
    Product updateEntityFromDto(ProductUpdateDto dto, @MappingTarget Product product);

//Entity -> Response
//-------------------------------------//
    ProductDto toDto(Product product);
    ProductSummaryDto toSummaryDto(Product product);
}
