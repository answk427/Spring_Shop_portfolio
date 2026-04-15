package work.trade.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatus {

    @Builder
    public OrderStatus(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    @Id
    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}