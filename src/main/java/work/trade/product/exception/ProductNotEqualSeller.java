package work.trade.product.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class ProductNotEqualSeller extends BusinessException {
    public ProductNotEqualSeller() {
        super("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
}
