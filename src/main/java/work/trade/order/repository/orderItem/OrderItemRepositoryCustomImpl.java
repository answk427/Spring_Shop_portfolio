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

        JPAQuery<OrderItemSummaryDto> query = queryFactory
                .select(Projections.constructor(OrderItemSummaryDto.class,
                        orderItem.id,
                        orderItem.product.name,
                        Projections.constructor(OrderStatusDto.class,
                                orderItem.status.code,
                                orderItem.status.name,
                                orderItem.status.description),
                        orderItem.quantity,
                        orderItem.subtotalPrice,
                        orderItem.order.createdAt
                ))
                .from(orderItem)
                .join(orderItem.product, product)
                .join(orderItem.order, order)
                .join(orderItem.status, orderStatus)
                .where(order.buyer.id.eq(buyerId),
                        statusEq(statusCode, orderStatus))
                .orderBy(order.createdAt.desc());

        if (pageable.isPaged()) {
            query.offset(pageable.getOffset())
                    .limit(pageable.getPageSize());
        }

        List<OrderItemSummaryDto> content = query.fetch();

        //카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(orderItem.count())
                .from(orderItem)
                .join(orderItem.order, order)
                .join(orderItem.status, orderStatus)
                .where(order.buyer.id.eq(buyerId),
                        statusEq(statusCode, orderStatus));

        //Page 객체로 반환 (PageableExecutionUtils를 쓰면 카운트 쿼리 최적화 가능)
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(String statusCode, QOrderStatus orderStatus) {
        return StringUtils.hasText(statusCode) ? orderStatus.code.eq(statusCode) : null;
    }
}
