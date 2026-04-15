package work.trade.cart.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class CartEmptyException extends BusinessException {
    public CartEmptyException() {
        super("장바구니가 비어있습니다.", HttpStatus.NOT_FOUND);
    }
}
