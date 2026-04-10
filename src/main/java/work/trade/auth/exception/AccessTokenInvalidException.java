package work.trade.auth.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class AccessTokenInvalidException extends BusinessException {
    public AccessTokenInvalidException() {
        super("AccessToken이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
    }
}
