package work.trade.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import work.trade.product.domain.ProductImage;
import work.trade.product.dto.response.ProductImageDto;

@Mapper(componentModel = "spring")
public abstract class ProductImageMapper {

    @Value("${file.display.prefix}")
    protected String imagePrefix;

    @Mapping(target = "imageUrl", source = "productImage.imageUrl", qualifiedByName = "toFullUrl")
    public abstract ProductImageDto toDto(ProductImage productImage);

    @Named("toFullUrl")
    protected String toFullUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }

        //이미 풀경로일경우 방어
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }

        return imagePrefix + (imagePrefix.endsWith("/") ? "" : "/") + imageUrl;
    }
}
