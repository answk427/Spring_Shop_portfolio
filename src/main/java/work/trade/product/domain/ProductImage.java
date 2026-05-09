package work.trade.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Builder
    private ProductImage(Product product, String imageUrl, Boolean thumbnail, Integer displayOrder) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.thumbnail = thumbnail;
        this.displayOrder = displayOrder;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean thumbnail = false;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected void setProduct(Product product) {
        this.product = product;
    }
}