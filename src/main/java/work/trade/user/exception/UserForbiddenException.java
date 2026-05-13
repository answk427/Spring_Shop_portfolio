package work.trade.user.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class UserForbiddenException extends BusinessException {
    public UserForbiddenException() {
        super("권한이 없는 유저의 접근입니다.", HttpStatus.FORBIDDEN);
    }
}
