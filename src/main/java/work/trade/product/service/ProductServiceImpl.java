package work.trade.product.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.exception.ProductNotFoundException;
import work.trade.product.mapper.ProductMapper;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.user.domain.User;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    
    private final ProductMapper mapper;

    @Override
    public ProductDto createProduct(ProductCreateRequestDto dto) {
        User seller = userRepository.findById(dto.getSellerId()).orElseThrow(() -> new UserNotFoundException());
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new RuntimeException("카테고리 없음"));

        Product product = mapper.toEntity(dto, seller, category);

        Product savedProduct = productRepository.save(product);
        return mapper.toDto(savedProduct);
    }

    @Override
    public Optional<ProductDto> findProduct(Long id) {
        return productRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public ProductDto updateProduct(ProductUpdateDto dto) {
        Product product = productRepository.findById(dto.getId()).orElseThrow(() -> new IllegalStateException("제품 없음: " + dto.getId()));
        if (!product.getId().equals(dto.getId())) {
            throw new ProductNotFoundException();
        }

        Category updateCategory = categoryRepository.findById(dto.getCategoryId()).orElse(product.getCategory());
        product.updateFromDto(dto, updateCategory);

        return mapper.toDto(product);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
