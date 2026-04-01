package work.trade.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.service.ProductService;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class productController {
    private final ProductService productService;

    //상품 등록
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @RequestBody @Valid ProductCreateRequestDto dto,
            Authentication authentication) {
        Long sellerId = Long.parseLong(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(dto, sellerId));
    }

    //상품 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.findProduct(id));
    }

    //상품 수정
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody @Valid ProductUpdateDto dto,
            Authentication authentication) {
        Long sellerId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(productService.updateProduct(dto, sellerId));
    }

    //상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id,
            Authentication authentication) {
        Long sellerId = Long.parseLong(authentication.getName());
        productService.deleteById(id, sellerId);
        return ResponseEntity.noContent().build();
    }
}
