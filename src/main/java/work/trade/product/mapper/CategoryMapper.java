package work.trade.product.mapper;

import org.mapstruct.Mapper;
import work.trade.product.domain.Category;
import work.trade.product.dto.response.CategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryDto dto);
    CategoryDto toDto(Category category);
}
