package work.trade.product.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.product.domain.Category;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;

    public static CategoryDto from(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());

        Category parent = category.getParent();
        if (parent != null) {
            categoryDto.setParentId(parent.getId());
            categoryDto.setName(parent.getName());
        }
        return categoryDto;
    }
}
