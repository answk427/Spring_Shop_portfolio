package work.trade.order.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class OrderCannotCancelException extends BusinessException {
    public OrderCannotCancelException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
