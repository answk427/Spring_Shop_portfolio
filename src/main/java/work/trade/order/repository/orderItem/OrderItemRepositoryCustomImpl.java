package work.trade.order.repository.orderItem;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import work.trade.order.domain.QOrderStatus;
import work.trade.order.dto.response.order.OrderStatusDto;
import work.trade.order.dto.response.orderItem.OrderItemSummaryDto;

import java.util.List;

import static work.trade.order.domain.QOrder.order;
import static work.trade.order.domain.QOrderItem.orderItem;
import static work.trade.order.domain.QOrderStatus.orderStatus;
import static work.trade.product.domain.QProduct.product;

@RequiredArgsConstructor
public class OrderItemRepositoryCustomImpl implements OrderItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderItemSummaryDto> findOrderItemsWithPagination(Long buyerId, String statusCode, Pageable pageable) {
        BooleanExpression condition = order.buyer.id.eq(buyerId)
                .and(statusEq(statusCode, orderStatus));
        return executePagedQuery(condition, pageable);
    }

    @Override
    public Page<OrderItemSummaryDto> findSellerOrderItemsWithPagination(Long sellerId, String statusCode, Pageable pageable) {
        BooleanExpression condition = product.seller.id.eq(sellerId)
                .and(statusEq(statusCode, orderStatus));
        return executePagedQuery(condition, pageable);
    }

    private Page<OrderItemSummaryDto> executePagedQuery(BooleanExpression condition, Pageable pageable) {
        // 1. 공통 쿼리 정의 (Content와 Count에서 공유할 Join 및 Where)
        // 판매자 ID 조건을 처리하기 위해 product 조인을 필수로 포함시킵니다.
        JPAQuery<?> baseQuery = queryFactory
                .from(orderItem)
                .join(orderItem.product, product)
                .join(orderItem.order, order)
                .join(orderItem.status, orderStatus)
                .where(condition);

        // 2. Content 쿼리: baseQuery 정보를 기반으로 select/orderBy/offset/limit 추가
        JPAQuery<OrderItemSummaryDto> contentQuery = baseQuery
                .select(Projections.constructor(OrderItemSummaryDto.class,
                        orderItem.id,
                        order.id,
                        product.name,
                        Projections.constructor(OrderStatusDto.class,
                                orderStatus.code,
                                orderStatus.name,
                                orderStatus.description),
                        orderItem.quantity,
                        orderItem.subtotalPrice,
                        order.createdAt
                ))
                .orderBy(order.createdAt.desc());

        if (pageable.isPaged()) {
            contentQuery.offset(pageable.getOffset())
                    .limit(pageable.getPageSize());
        }

        List<OrderItemSummaryDto> content = contentQuery.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(orderItem.count())
                .from(orderItem)
                .join(orderItem.product, product)
                .join(orderItem.order, order)
                .join(orderItem.status, orderStatus)
                .where(condition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(String statusCode, QOrderStatus orderStatus) {
        return StringUtils.hasText(statusCode) ? orderStatus.code.eq(statusCode) : null;
    }
}
