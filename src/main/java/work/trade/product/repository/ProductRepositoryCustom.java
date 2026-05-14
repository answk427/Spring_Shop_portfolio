package work.trade.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import work.trade.product.dto.response.ProductSummaryDto;

import java.util.List;

public interface ProductRepositoryCustom {
    Page<ProductSummaryDto> findProductsWithPagination(Pageable pageable, List<Long> categoryIds, Long sellerId);

    Page<ProductSummaryDto> searchProducts(List<Long> categoryIds, String keyword, Pageable pageable);
}
