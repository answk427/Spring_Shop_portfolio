package work.trade.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateDto {

    private Long categoryId;

    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
    private String name;

    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다.")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    private BigDecimal price;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private Integer stock;
}