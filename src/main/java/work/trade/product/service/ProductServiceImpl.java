package work.trade.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import work.trade.file.service.FileUploadService;
import work.trade.file.util.ProductImageFileUrlResolver;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.domain.ProductImage;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.product.exception.CategoryNotFoundException;
import work.trade.product.exception.ProductNotEqualSeller;
import work.trade.product.exception.ProductNotFoundException;
import work.trade.product.mapper.ProductMapper;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductImageRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.user.domain.User;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.repository.UserRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    private final FileUploadService fileUploadService;
    private final ProductImageService productImageService;

    private final ProductMapper mapper;
    private final ProductImageFileUrlResolver fileUrlResolver;

//*******************************//

    private void saveProductImage(Product product, MultipartFile imageFile, boolean isThumbnail, int displayOrder) throws FileUploadException {
        String imagePath = fileUploadService.uploadFile(imageFile, "products");

        ProductImage productImage = ProductImage.builder()
                .thumbnail(isThumbnail)
                .imageUrl(imagePath)
                .displayOrder(displayOrder)
                .build();

        //연관관계 설정
        product.addProductImage(productImage);
    }

    private PageImpl<ProductSummaryDto> convertSummaryDtoUrl(Pageable pageable, Page<ProductSummaryDto> page) {
        List<ProductSummaryDto> converted = page.getContent().stream()
                .map(dto -> new ProductSummaryDto(
                        dto.id(),
                        dto.name(),
                        dto.price(),
                        dto.stock(),
                        dto.categoryName(),
                        dto.sellerName(),
                        fileUrlResolver.resolve(dto.thumbnailUrl()),
                        dto.createdAt()
                ))
                .toList();

        return new PageImpl<>(converted, pageable, page.getTotalElements());
    }
//*******************************//

    @Override
    public ProductDto createProduct(ProductCreateRequestDto dto, Long sellerId, MultipartFile thumbnail, List<MultipartFile> images) throws FileUploadException {
        log.info("Start createProductWithImage sellerId: {}, productName: {}", sellerId, dto.getName());
        User seller = userRepository.findById(sellerId).orElseThrow(() -> new UserNotFoundException());

        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new CategoryNotFoundException());

        //Product 생성
        Product product = mapper.toEntity(dto, seller, category);
        //썸네일 우선 저장
        if (thumbnail != null && !thumbnail.isEmpty()) {
            saveProductImage(product, thumbnail, true, 0);
        }
        //나머지 이미지 저장
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); ++i) {
                saveProductImage(product, images.get(i), false, i+1);
            }
        }
        //Product 저장과 동시에 이미지도 저장(cascade)
        Product savedProduct = productRepository.save(product);

        log.info("Complete createProductWithImage productName: {}, productId: {}", dto.getName(), product.getId());
        return mapper.toDto(savedProduct);
    }

    @Override
    public ProductDto createProduct(ProductCreateRequestDto dto, Long sellerId) {
        log.info("Start createProduct sellerId: {}, productName: {}", sellerId, dto.getName());
        User seller = userRepository.findById(sellerId).orElseThrow(() -> new UserNotFoundException());

        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new CategoryNotFoundException());

        Product product = mapper.toEntity(dto, seller, category);

        Product savedProduct = productRepository.save(product);

        log.info("Complete createProduct productName: {}, productId: {}", dto.getName(), product.getId());
        return mapper.toDto(savedProduct);
    }

    @Override
    public ProductDto findProduct(Long id) {
        return productRepository.findByIdFetchJoin(id).
                map(mapper::toDto)
                .orElseThrow(() -> new ProductNotFoundException());
    }

    @Override
    public Page<ProductSummaryDto> findProducts(Pageable pageable) {
        Page<ProductSummaryDto> page = productRepository.findProductsWithPagination(pageable, null, null);
        return convertSummaryDtoUrl(pageable, page);
    }

    @Override
    public Page<ProductSummaryDto> findProductsByCategory(Pageable pageable, Long categoryId) {
        categoryRepository.findById(categoryId).
                orElseThrow(() -> new CategoryNotFoundException());

        List<Long> categoryIds = categoryRepository.findAllChildCategoryIds(categoryId);

        Page<ProductSummaryDto> page = productRepository.findProductsWithPagination(pageable, categoryIds, null);
        return convertSummaryDtoUrl(pageable, page);
    }

    @Override
    public Page<ProductSummaryDto> findProductsBySellerId(Pageable pageable, Long sellerId) {
        Page<ProductSummaryDto> page = productRepository.findProductsWithPagination(pageable, null, sellerId);
        return convertSummaryDtoUrl(pageable, page);
    }

    @Override
    public ProductDto updateProduct(Long productId, Long sellerId, ProductUpdateDto dto, MultipartFile thumbnail, List<MultipartFile> newImages) throws FileUploadException {
        log.info("Start update Product. sellerId: {}, productId: {}", sellerId, productId);
        Product product = productRepository.findByIdFetchJoin(productId).orElseThrow(() -> new ProductNotFoundException());

        //판매자 일치 체크
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ProductNotEqualSeller();
        }

        //카테고리 유효성 체크
        Category updateCategory;

        if (dto.getCategoryId() != null) {
            updateCategory = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(CategoryNotFoundException::new);
        } else {
            updateCategory = product.getCategory();
        }

        product.updateFromDto(dto, updateCategory);

        // 기존 이미지 삭제
        if (dto.getDeleteImageIds() != null && !dto.getDeleteImageIds().isEmpty()) {
            List<String> oldImagePaths = productImageService.getImagePathList(productId, dto.getDeleteImageIds());
            //실제 저장된 파일 삭제
            for (String imagePath : oldImagePaths) {
                fileUploadService.deleteFile(imagePath);
            }

            //연관관계 삭제
            product.getProductImages()
                    .removeIf(img -> dto.getDeleteImageIds().contains(img.getId()));
        }

        //썸네일 추가
        if (thumbnail != null && !thumbnail.isEmpty()) {
            saveProductImage(product, thumbnail, true, 0);
        }
        // 새로운 이미지 추가
        if (newImages != null && !newImages.isEmpty()) {
            for (int i=0; i<newImages.size(); ++i) {
                saveProductImage(product, newImages.get(i), false, i + 1);
            }
        }

        productRepository.flush();

        log.info("Complete update Product. sellerId: {}, productId: {}", sellerId, productId);
        return mapper.toDto(product);
    }

    @Override
    public void deleteById(Long id, Long sellerId) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException());

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ProductNotEqualSeller();
        }

        productRepository.deleteById(id);
    }

    @Override
    public Page<ProductSummaryDto> searchProducts(Long categoryId, String keyword, Pageable pageable) {
        List<Long> categoryIds = categoryRepository.findAllChildCategoryIds(categoryId);
        Page<ProductSummaryDto> page = productRepository.searchProducts(categoryIds, keyword, pageable);
        return convertSummaryDtoUrl(pageable, page);
    }
}
