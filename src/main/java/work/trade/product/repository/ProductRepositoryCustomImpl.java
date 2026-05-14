package work.trade.product.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import work.trade.product.domain.QProductImage;
import work.trade.product.dto.response.ProductSummaryDto;

import java.util.List;

import static work.trade.product.domain.QCategory.category;
import static work.trade.product.domain.QProduct.product;
import static work.trade.user.domain.QUser.user;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

//******동적 조건 함수 - null이면 무시*************************//

    private Predicate matchKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        //TEXT FULL 인덱스를 활용해 검색
        return Expressions.numberTemplate(
                Double.class,
                "function('match_against', {0}, {1}, {2})",
                product.name,
                product.description,
                keyword
        ).gt(0);
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        return sellerId != null ? product.seller.id.eq(sellerId) : null;
    }

    private BooleanExpression categoryIdEq(List<Long> categoryIds) {
        return categoryIds != null && !categoryIds.isEmpty() ?
                product.category.id.in(categoryIds) : null;
    }

//*******************************//

    @Override
    public Page<ProductSummaryDto> findProductsWithPagination(Pageable pageable, List<Long> categoryIds, Long sellerId) {
        //서브쿼리 구분 위해 객체 생성
        QProductImage subImage = new QProductImage("subImage");

        JPQLQuery<String> thumbnailSubQuery = JPAExpressions
                .select(subImage.imageUrl)
                .from(subImage)
                .where(subImage.product.eq(product)
                        .and(subImage.thumbnail.isTrue()))
                .limit(1);

        // 데이터 조회 (Select절에서 바로 DTO 가져오기)
        List<ProductSummaryDto> content = queryFactory
                .select(Projections.constructor(ProductSummaryDto.class,
                        product.id,          // 1. Long id
                        product.name,        // 2. String name
                        product.price,       // 3. BigDecimal price
                        product.stock,       // 4. Integer stock
                        category.name,       // 5. String categoryName
                        user.name,           // 6. String sellerName
                        thumbnailSubQuery,
                        product.createdAt    // 7. LocalDateTime createdAt
                ))
                .from(product)
                .join(product.category, category)
                .join(product.seller, user)
                .where(
                        categoryIdEq(categoryIds),
                        sellerIdEq(sellerId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(categoryIdEq(categoryIds), sellerIdEq(sellerId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ProductSummaryDto> searchProducts(List<Long> categoryIds, String keyword, Pageable pageable) {
        //서브쿼리 구분 위해 객체 생성
        QProductImage subImage = new QProductImage("subImage");

        JPQLQuery<String> thumbnailSubQuery = JPAExpressions
                .select(subImage.imageUrl)
                .from(subImage)
                .where(subImage.product.eq(product)
                        .and(subImage.thumbnail.isTrue()))
                .limit(1);

        List<ProductSummaryDto> content = queryFactory
                .select(Projections.constructor(ProductSummaryDto.class,
                        product.id,          // 1. Long id
                        product.name,        // 2. String name
                        product.price,       // 3. BigDecimal price
                        product.stock,       // 4. Integer stock
                        category.name,       // 5. String categoryName
                        user.name,           // 6. String sellerName
                        thumbnailSubQuery,
                        product.createdAt    // 7. LocalDateTime createdAt
                ))
                .from(product)
                .join(product.category, category)
                .join(product.seller, user)
                .where(
                        categoryIdEq(categoryIds),
                        matchKeyword(keyword)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .join(product.category, category)
                .where(
                        categoryIdEq(categoryIds),
                        matchKeyword(keyword)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
