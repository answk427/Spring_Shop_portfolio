package work.trade.product.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super("카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
