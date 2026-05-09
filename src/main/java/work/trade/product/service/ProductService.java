package work.trade.product.service;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductSummaryDto;

import java.util.List;


public interface ProductService {

    //CRUD
    ProductDto createProduct(ProductCreateRequestDto dto, Long sellerId, List<MultipartFile> images) throws FileUploadException;
    ProductDto createProduct(ProductCreateRequestDto dto, Long sellerId);

    ProductDto findProduct(Long id);
    Page<ProductSummaryDto> findProducts(Pageable pageable);
    Page<ProductSummaryDto> findProductsByCategory(Pageable pageable, Long categoryId);
    Page<ProductSummaryDto> findProductsBySellerId(Pageable pageable, Long sellerId);

    ProductDto updateProduct(Long productId, Long sellerId, ProductUpdateDto dto, List<MultipartFile> newImages) throws FileUploadException;
    void deleteById(Long id, Long sellerId);

    Page<ProductSummaryDto> searchProducts(Long categoryId, String keyword, Pageable pageable);
}

