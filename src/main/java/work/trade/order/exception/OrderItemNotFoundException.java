package work.trade.order.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class OrderItemNotFoundException extends BusinessException {
    public OrderItemNotFoundException() {
        super("주문상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

}
