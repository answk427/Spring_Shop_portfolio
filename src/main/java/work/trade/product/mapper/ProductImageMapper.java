package work.trade.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import work.trade.file.util.FileUrlResolver;
import work.trade.product.domain.ProductImage;
import work.trade.product.dto.response.ProductImageDto;

@Mapper(componentModel = "spring")
public abstract class ProductImageMapper {

    @Autowired
    protected FileUrlResolver fileUrlResolver;

    @Mapping(target = "imageUrl", source = "productImage.imageUrl", qualifiedByName = "toFullUrl")
    public abstract ProductImageDto toDto(ProductImage productImage);

    @Named("toFullUrl")
    protected String toFullUrl(String imageUrl) {
        return fileUrlResolver.resolve(imageUrl);
    }
}
