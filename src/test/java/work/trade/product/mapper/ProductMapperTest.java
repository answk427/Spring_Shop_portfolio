package work.trade.product.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import work.trade.product.domain.Category;
import work.trade.product.domain.Product;
import work.trade.product.dto.request.ProductCreateRequestDto;
import work.trade.product.dto.request.ProductUpdateDto;
import work.trade.product.dto.response.ProductDto;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductMapperTest {

    @Autowired
    private ProductMapper mapper;

    Product getTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("테스트제품");
        product.setDescription("제품설명");
        product.setPrice(BigDecimal.valueOf(111));
        product.setStock(22);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Category category = new Category();
        category.setId(222L);
        category.setName("테스트카테고리");
        category.setParent(null);

        product.setCategory(category);

        User seller = new User();
        seller.setId(2234L);
        seller.setName("판매자");
        product.setSeller(seller);

        return product;
    }

    @Test
    void toEntity() {
        //given
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setName("새상품");
        dto.setPrice(BigDecimal.valueOf(10000));
        dto.setStock(1232);
        dto.setDescription("새상품 설명");
        dto.setSellerId(123L);
        dto.setCategoryId(3131L);

        //when
        Product entity = mapper.toEntity(dto);

        //then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();

        //Service에서 처리
        assertThat(entity.getSeller()).isNull();
        assertThat(entity.getCategory()).isNull();

        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getPrice()).isEqualTo(dto.getPrice());
        assertThat(entity.getStock()).isEqualTo(dto.getStock());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
    }

    @Test
    void updateEntityFromDto() {
        //given
        Product product = getTestProduct();
        Long oldId = product.getId();

        ProductUpdateDto dto = new ProductUpdateDto();

        dto.setId(11L);
        dto.setCategoryId(1311L);
        dto.setName("테스트 dto");
        dto.setDescription("업데이트 설명");
        dto.setPrice(BigDecimal.valueOf(1311));
        dto.setStock(111);

        //when
        mapper.updateEntityFromDto(dto, product);

        //then
        //id는 복사x
        assertThat(product.getId()).isEqualTo(oldId);
        assertThat(product.getName()).isEqualTo(dto.getName());
        assertThat(product.getDescription()).isEqualTo(dto.getDescription());
        assertThat(product.getPrice()).isEqualTo(dto.getPrice());
        assertThat(product.getStock()).isEqualTo(dto.getStock());

        assertThat(product.getCategory()).isNotNull();
        }

    @Test
    void toDto() {
        //given
        Product product = getTestProduct();

        //when
        ProductDto dto = mapper.toDto(product);

        //then
        assertThat(dto.getId()).isEqualTo(product.getId());
        assertThat(dto.getName()).isEqualTo(product.getName());
        assertThat(dto.getDescription()).isEqualTo(product.getDescription());
        assertThat(dto.getPrice()).isEqualTo(product.getPrice());
        assertThat(dto.getStock()).isEqualTo(product.getStock());
        assertThat(dto.getCreatedAt()).isEqualTo(product.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(product.getUpdatedAt());


        assertThat(dto.getCategory()).isNotNull();
        assertThat(dto.getSeller()).isNotNull();
        assertThat(dto.getSeller().getId()).isEqualTo(product.getSeller().getId());
    }

    @Test
    void toSummaryDto() {
        //given
        Product product = getTestProduct();

        //when
        ProductSummaryDto dto = mapper.toSummaryDto(product);

        //then
        assertThat(dto.getId()).isEqualTo(product.getId());
        assertThat(dto.getName()).isEqualTo(product.getName());
        assertThat(dto.getPrice()).isEqualTo(product.getPrice());
        assertThat(dto.getStock()).isEqualTo(product.getStock());
        assertThat(dto.getCreatedAt()).isEqualTo(product.getCreatedAt());

        //service에서 처리
        assertThat(dto.getCategoryName()).isNull();
        assertThat(dto.getSellerName()).isNull();
    }
}