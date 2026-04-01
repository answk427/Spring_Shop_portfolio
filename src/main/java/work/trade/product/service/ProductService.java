package work.trade.product.service;

import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.request.ProductUpdateDto;

import java.util.Optional;

public interface ProductService {

    //CRUD
    ProductDto createProduct(ProductCreateRequestDto dto, Long sellerId);
    ProductDto findProduct(Long id);
    ProductDto updateProduct(ProductUpdateDto dto, Long sellerId);
    void deleteById(Long id, Long sellerId);
}

