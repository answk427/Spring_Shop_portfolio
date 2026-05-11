package work.trade.product.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.product.domain.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findByProductIdAndThumbnailTrue(Long productId);

    //여러 상품의 썸네일 한번에 가져오기
    List<ProductImage> findByProductIdInAndThumbnailTrue(List<Long> productIds);

    //썸네일이 아닌 상세 이미지들을 순서대로 페이징(Slice) 조회
    Slice<ProductImage> findByProductIdAndThumbnailFalseOrderByDisplayOrderAsc(Long productId, Pageable pageable);

    List<ProductImage> findByIdInAndProduct_Id(List<Long> imageIds, Long productId);
}
