package work.trade.product.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class ProductImageNotFoundException extends BusinessException {
    public ProductImageNotFoundException() {
        super("상품 이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
