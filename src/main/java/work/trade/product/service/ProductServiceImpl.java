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

        Product product = mapper.toEntity(dto);
        product.setSeller(seller);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return mapper.toDto(savedProduct);
    }

    @Override
    public Optional<ProductDto> findProduct(Long id) {
        return productRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public ProductDto updateProduct(ProductUpdateDto dto) {
        //추후 예외 수정
        Product product = productRepository.findById(dto.getId()).orElseThrow(() -> new IllegalStateException("제품 없음: " + dto.getId()));
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new IllegalStateException("카테고리 없음"));

        mapper.updateEntityFromDto(dto, product);
        product.setCategory(category);

        return mapper.toDto(product);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
