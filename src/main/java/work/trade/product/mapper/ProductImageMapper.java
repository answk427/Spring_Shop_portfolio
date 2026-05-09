package work.trade.product.mapper;

import org.mapstruct.Mapper;
import work.trade.product.domain.ProductImage;
import work.trade.product.dto.response.ProductImageDto;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {
    ProductImageDto toDto(ProductImage productImage);
}
