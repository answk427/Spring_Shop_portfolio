package work.trade.cart.dto.response;

import work.trade.product.dto.response.ProductSummaryDto;

import java.time.LocalDateTime;

public record CartDto (Long id,
        ProductSummaryDto product,
        Integer quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt){

}

