package work.trade.wallet.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class WalletNotFoundException extends BusinessException {
    public WalletNotFoundException(Long userId) {
        super(String.format("userId:[%d]의 Wallet을 찾을 수 없습니다.", userId),
                HttpStatus.NOT_FOUND);
    }
}
