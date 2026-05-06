package work.trade.wallet.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class WalletInsufficientBalanceException extends BusinessException {
    public WalletInsufficientBalanceException() {
        super("잔액이 부족합니다.", HttpStatus.BAD_REQUEST);
    }
}
