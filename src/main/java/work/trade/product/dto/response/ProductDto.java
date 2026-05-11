package work.trade.product.dto.response;

import work.trade.user.dto.response.SellerDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDto(
        Long id,
        SellerDto seller,
        CategoryDto category,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ProductImageDto thumbnail,
        List<ProductImageDto> images
) {

}

