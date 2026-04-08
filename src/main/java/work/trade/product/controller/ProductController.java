package work.trade.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.product.service.ProductService;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
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
        return ResponseEntity.ok(productService.updateProduct(dto, id, sellerId));
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

    //전체 상품 조회
    @GetMapping
    public ResponseEntity<Page<ProductSummaryDto>> getProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.findProducts(pageable));
    }

    //카테고리별 상품 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductSummaryDto>> getProductsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.findProductsByCategory(pageable, categoryId));
    }

    //내 상품 목록 조회 - 로그인 필요
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<Page<ProductSummaryDto>> getMyProducts(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        long sellerId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(productService.findProductsBySellerId(pageable, sellerId));
    }
}
