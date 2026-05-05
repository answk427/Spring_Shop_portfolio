package work.trade.wallet.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class WalletDuplicateException extends BusinessException {

    public WalletDuplicateException() {
        super("계정에 이미 지갑이 존재합니다.", HttpStatus.CONFLICT);
    }
}
