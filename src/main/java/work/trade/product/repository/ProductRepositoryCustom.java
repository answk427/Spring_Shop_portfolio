package work.trade.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import work.trade.product.dto.response.ProductSummaryDto;

public interface ProductRepositoryCustom {
    Page<ProductSummaryDto> findProductsWithPagination(Pageable pageable, Long categoryId, Long sellerId);

    Page<ProductSummaryDto> searchProducts(Long categoryId, String keyword, Pageable pageable);
}
