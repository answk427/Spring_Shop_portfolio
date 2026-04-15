package work.trade.product.domain;

import jakarta.persistence.*;
import lombok.*;
import work.trade.order.exception.InsufficientStockException;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Builder
    private Product(User seller, Category category, String name, String description, BigDecimal price, Integer stock) {
        this.seller = seller;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "seller_id", nullable = false)
    private User seller;

    //카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    //레코드 생성/업데이트 시 자동갱신
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;


    public void updateFromDto(ProductUpdateDto dto, Category category) {
        //카테고리는 서비스에서 검증
        if (dto.getName() != null)  this.name = dto.getName();
        if (dto.getPrice() != null)  this.price = dto.getPrice();
        if (dto.getStock() != null)  this.stock = dto.getStock();
        if (dto.getDescription() != null)  this.description = dto.getDescription();
        if (category != null)  this.category = category;
    }

    public void decreaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1이상이어야 합니다.");
        }

        if (this.stock < quantity) {
            throw new InsufficientStockException(this.name,
                    quantity,
                    this.stock);
        }

        this.stock -= quantity;
    }

    /**
     * 재고 증가
     * 주문 취소 시 호출
     */
    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.stock += quantity;
    }

    public boolean hasEnoughStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        return this.stock >= quantity;
    }

}
