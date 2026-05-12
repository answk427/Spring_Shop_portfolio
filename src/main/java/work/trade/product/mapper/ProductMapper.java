package work.trade.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.domain.ProductImage;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductImageDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.user.domain.User;
import work.trade.user.mapper.UserMapper;

@Mapper(componentModel = "spring",
        uses = {
                CategoryMapper.class,
                UserMapper.class,
                ProductImageMapper.class})
public abstract class ProductMapper {

    @Autowired
    protected ProductImageMapper imageMapper;

    //Request -> Entity
//-------------------------------------//
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "price", source = "dto.price")
    @Mapping(target = "stock", source = "dto.stock")
    @Mapping(target = "seller", source = "seller") // 두 번째 인자 User 객체 통째로
    @Mapping(target = "category", source = "category")
    // 세 번째 인자 Category 객체 통째로
    public abstract Product toEntity(ProductCreateRequestDto dto, User seller, Category category);

    //Entity -> Response
//-------------------------------------//
    @Mapping(target = "thumbnail", source = ".", qualifiedByName = "toThumbnail")
    public abstract ProductDto toDto(Product product);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "sellerName", source = "seller.name")
    @Mapping(target = "thumbnailUrl", source = ".", qualifiedByName = "toThumbnailUrl")
    public abstract ProductSummaryDto toSummaryDto(Product product);

    @Named("toThumbnail")
    protected ProductImageDto toThumbnail(Product product) {
        return product.getProductImages().stream()
                .filter(ProductImage::getThumbnail)
                .findFirst()
                .map(imageMapper::toDto)
                .orElse(null);
    }

    @Named("toThumbnailUrl")
    protected String toThumbnailUrl(Product product) {
        ProductImageDto thumbnail = toThumbnail(product);
        return thumbnail != null ? thumbnail.imageUrl() : null;
    }
}
