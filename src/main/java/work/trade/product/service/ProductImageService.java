package work.trade.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.product.domain.ProductImage;
import work.trade.product.dto.response.ProductImageDto;
import work.trade.product.exception.ProductImageNotFoundException;
import work.trade.product.mapper.ProductImageMapper;
import work.trade.product.repository.ProductImageRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductImageMapper mapper;
//*******************************//

    public String getThumbnailUrl(Long productId) {
        return productImageRepository.findByProductIdAndThumbnailTrue(productId)
                .map(ProductImage::getImageUrl)
                .orElse("default-thumbnail.png");
    }

    public Map<Long, ProductImage> getThumbnailUrlInBatch(List<Long> productIds) {
        List<ProductImage> images = productImageRepository.findByProductIdInAndThumbnailTrue(productIds);

        //product Id와 매핑
        return images.stream().collect(Collectors.toMap(
                img -> img.getProduct().getId(),
                img -> img
        ));
    }

    public Slice<ProductImageDto> getDetailImagesSlice(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Slice<ProductImage> imageSlice = productImageRepository
                .findByProductIdAndThumbnailFalseOrderByDisplayOrderAsc(productId, pageable);

        return imageSlice.map(mapper::toDto);
    }

    public List<ProductImageDto> getDetailImagesList(Long productId, List<Long> imageIds) {
        List<ProductImage> productImages = productImageRepository.findByIdInAndProduct_Id(imageIds, productId);

        if (productImages.size() != imageIds.size()) {
            throw new ProductImageNotFoundException();
        }

        return productImages.stream().map(mapper::toDto).toList();
    }

    public List<String> getImagePathList(Long productId, List<Long> imageIds) {
        return productImageRepository
                .findByIdInAndProduct_Id(imageIds, productId)
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();
    }

    public ProductImageDto createProductImage(ProductImage image) {
        return mapper.toDto(productImageRepository.save(image));
    }

    public void deleteInBatch(List<Long> imageIds) {
        productImageRepository.deleteAllByIdInBatch(imageIds);
    }
}
