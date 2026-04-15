package work.trade.order.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException() {
        super("주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
