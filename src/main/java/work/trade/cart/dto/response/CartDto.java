package work.trade.cart.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.product.dto.response.ProductSummaryDto;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CartDto {
    private Long id;
    private ProductSummaryDto product;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
