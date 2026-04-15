package work.trade.order.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class OrderStatusNotFoundException extends BusinessException {
    public OrderStatusNotFoundException() {
        super("주문상태를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
