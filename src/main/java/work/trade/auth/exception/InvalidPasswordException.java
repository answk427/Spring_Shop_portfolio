package work.trade.auth.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException() {
        super("비밀번호가 틀렸습니다", HttpStatus.UNAUTHORIZED);
    }
}
