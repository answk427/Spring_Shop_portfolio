package work.trade.product.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import work.trade.product.domain.ProductImage;
import work.trade.product.dto.response.ProductImageDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T23:15:03+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class ProductImageMapperImpl implements ProductImageMapper {

    @Override
    public ProductImageDto toDto(ProductImage productImage) {
        if ( productImage == null ) {
            return null;
        }

        Long id = null;
        String imageUrl = null;
        Integer displayOrder = null;

        id = productImage.getId();
        imageUrl = productImage.getImageUrl();
        displayOrder = productImage.getDisplayOrder();

        ProductImageDto productImageDto = new ProductImageDto( id, imageUrl, displayOrder );

        return productImageDto;
    }
}
