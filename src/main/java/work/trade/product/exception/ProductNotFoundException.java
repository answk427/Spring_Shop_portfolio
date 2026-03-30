package work.trade.product.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException() {
        super("존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND);
    }
}
