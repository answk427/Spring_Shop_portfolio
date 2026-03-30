package work.trade.user.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class AuthProviderNotFoundException extends BusinessException {
    public AuthProviderNotFoundException() {
        super("찾을 수 없는 로그인 방식", HttpStatus.NOT_FOUND);
    }
}
