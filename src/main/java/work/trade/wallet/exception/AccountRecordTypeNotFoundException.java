package work.trade.wallet.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class AccountRecordTypeNotFoundException extends BusinessException {

    public AccountRecordTypeNotFoundException() {
        super("찾을 수 없는 지갑 기록내역 타입입니다.", HttpStatus.NOT_FOUND);
    }
}
