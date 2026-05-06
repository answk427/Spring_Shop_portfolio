package work.trade.wallet.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class WalletAmountException extends BusinessException {
    public WalletAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
