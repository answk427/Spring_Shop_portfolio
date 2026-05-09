package work.trade.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductSummaryDto(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        String categoryName,
        String sellerName,
        String thumbnailUrl,
        LocalDateTime createdAt
) {

}