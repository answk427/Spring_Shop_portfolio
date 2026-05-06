package work.trade.order.repository.order;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import work.trade.order.dto.response.order.OrderSummaryDto;

import java.util.List;

import static work.trade.order.domain.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderSummaryDto> findOrdersWithPagination(Long buyerId, Pageable pageable) {

        //데이터 조회 쿼리
        JPAQuery<OrderSummaryDto> query = queryFactory
                .select(Projections.constructor(OrderSummaryDto.class,
                        order.id,
                        order.totalPrice,
                        order.orderItems.size(),
                        order.createdAt
                ))
                .from(order)
                .where(order.buyer.id.eq(buyerId))
                .orderBy(order.createdAt.desc());

        if (pageable.isPaged()) {
            query.offset(pageable.getOffset())
                    .limit(pageable.getPageSize());
        }

        List<OrderSummaryDto> content = query.fetch();

        //카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(order.buyer.id.eq(buyerId));

        //Page 객체로 반환 (PageableExecutionUtils를 쓰면 카운트 쿼리 최적화 가능)
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}

