package work.trade.order.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String productName, int requestedQuantity, int availableStock) {
        super(String.format("상품 '%s'의 재고가 부족합니다. (요청: %d개, 남은 수량: %d개)",
                        productName, requestedQuantity, availableStock),
                HttpStatus.NOT_FOUND);
    }
}
