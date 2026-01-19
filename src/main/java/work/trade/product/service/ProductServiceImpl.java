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
import work.trade.product.mapper.ProductMapper;
import work.trade.product.repository.CategoryRepository;
import work.trade.product.repository.ProductRepository;
import work.trade.user.domain.User;
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
        //추후 커스텀 예외로 변경할 것
        User seller = userRepository.findById(dto.getSellerId()).orElseThrow(() -> new RuntimeException("유저 없음"));
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
        //커스텀예외로 추후 수정
        Product product = productRepository.findById(dto.getId()).orElseThrow(() -> new IllegalStateException("제품 없음: " + dto.getId()));
        if (!product.getId().equals(dto.getId())) {
            throw new IllegalStateException("수정하려는 제품의 ID와 일치하지 않음");
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
