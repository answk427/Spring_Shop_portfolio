package work.trade.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private OrderStatus(String code, String name, String description) {
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