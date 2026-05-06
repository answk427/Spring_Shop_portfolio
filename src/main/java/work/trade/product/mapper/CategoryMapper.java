package work.trade.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import work.trade.product.domain.Category;
import work.trade.product.dto.response.CategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    //parent 서비스에서 넣어주기
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryDto dto);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    CategoryDto toDto(Category category);
}
