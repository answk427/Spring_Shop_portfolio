package work.trade.user.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class UserUnAuthorizedException extends BusinessException {
    public UserUnAuthorizedException() {
        super("권한이 없는 유저의 접근입니다.", HttpStatus.UNAUTHORIZED);
    }
}
