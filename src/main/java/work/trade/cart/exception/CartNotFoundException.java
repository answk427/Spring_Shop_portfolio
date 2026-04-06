package work.trade.cart.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class CartNotFoundException extends BusinessException {
    public CartNotFoundException() {
        super("장바구니에 없는 데이터", HttpStatus.NOT_FOUND);
    }
}
