package work.trade.order.dto.response.order;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusDto {
    private String code;
    private String name;
    private String description;
}
