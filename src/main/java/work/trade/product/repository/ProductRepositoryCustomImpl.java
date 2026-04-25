package work.trade.product.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import work.trade.product.domain.QCategory;
import work.trade.product.domain.QProduct;
import work.trade.product.dto.response.ProductSummaryDto;
import work.trade.user.domain.QUser;

import java.util.List;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSummaryDto> findProductsWithPagination(Pageable pageable, Long categoryId, Long sellerId) {
        QProduct product = QProduct.product;
        QCategory category = QCategory.category;
        QUser user = QUser.user;

        // 데이터 조회 (Select절에서 바로 DTO 가져오기)
        List<ProductSummaryDto> content = queryFactory
                .select(Projections.constructor(ProductSummaryDto.class,
                        product.id,          // 1. Long id
                        product.name,        // 2. String name
                        product.price,       // 3. BigDecimal price
                        product.stock,       // 4. Integer stock
                        category.name,       // 5. String categoryName
                        user.name,           // 6. String sellerName
                        product.createdAt    // 7. LocalDateTime createdAt
                ))
                .from(product)
                .join(product.category, category)
                .join(product.seller, user)
                .where(
                        categoryIdEq(categoryId),
                        sellerIdEq(sellerId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(categoryIdEq(categoryId), sellerIdEq(sellerId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        return sellerId != null ? QProduct.product.seller.id.eq(sellerId) : null;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null? QProduct.product.category.id.eq(categoryId) : null;
    }


}
