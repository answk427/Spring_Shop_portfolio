package work.trade.product.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.product.domain.Category;
import work.trade.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductSummaryDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String categoryName;
    private String sellerName;
    private LocalDateTime createdAt;
}
