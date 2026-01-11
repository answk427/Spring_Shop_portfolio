package work.trade.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.user.dto.SellerDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductDto {
    private long id;

    private SellerDto seller;
    private CategoryDto category;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
