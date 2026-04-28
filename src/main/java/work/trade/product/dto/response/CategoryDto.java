package work.trade.product.dto.response;

public record CategoryDto(
        Long id,
        String name,
        Long parentId,
        String parentName
) {

}
