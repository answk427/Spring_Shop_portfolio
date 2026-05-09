package work.trade.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductImageDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.product.service.ProductImageService;
import work.trade.product.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductImageService productImageService;

    //상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> createProduct(
            @RequestPart(value = "dto") @Valid ProductCreateRequestDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) throws FileUploadException {
        Long sellerId = Long.parseLong(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(dto, sellerId, images));
    }

    //상품 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.findProduct(id));
    }


    //상품 수정
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") Long productId,
            @RequestPart(value = "dto") @Valid ProductUpdateDto dto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            Authentication authentication) throws FileUploadException {
        Long sellerId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(productService.updateProduct(productId, sellerId, dto, newImages));
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

    /**
     * 제품 상세 이미지 페이징 조회 (무한 스크롤 API)
     * 요청 예시: GET /api/products/1/detail-images?page=0&size=3
     * 정렬 순서를 강제하기 위해 Pageable 대신 page, size를 직접 받음
     */
    @GetMapping("/{productId}/detail-images")
    public ResponseEntity<Slice<ProductImageDto>> getDetailImages(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Slice<ProductImageDto> detailImages = productImageService.getDetailImagesSlice(productId, page, size);
        return ResponseEntity.ok(detailImages);
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

    //상품 검색 (카테고리 필터 포함)
    @GetMapping("/search")
    public ResponseEntity<Page<ProductSummaryDto>> searchProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(categoryId, keyword, pageable));
    }
}
