package work.trade.order.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class OrderInvalidStatusException extends BusinessException {
    public OrderInvalidStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
