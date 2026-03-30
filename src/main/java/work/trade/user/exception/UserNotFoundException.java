package work.trade.user.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("존재하지 않는 유저입니다", HttpStatus.NOT_FOUND);
    }
}
