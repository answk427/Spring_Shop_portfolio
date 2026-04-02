package work.trade.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductSummaryDto;


public interface ProductService {

    //CRUD
    ProductDto createProduct(ProductCreateRequestDto dto, Long sellerId);

    ProductDto findProduct(Long id);
    Page<ProductSummaryDto> findProducts(Pageable pageable);
    Page<ProductSummaryDto> findProductsByCategory(Pageable pageable, Long categoryId);
    Page<ProductSummaryDto> findProductsBySellerId(Pageable pageable, Long sellerId);


    ProductDto updateProduct(ProductUpdateDto dto, Long id, Long sellerId);
    void deleteById(Long id, Long sellerId);
}

