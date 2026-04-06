package work.trade.user.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class UserDuplicateEmailException extends BusinessException {
    public UserDuplicateEmailException() {
        super("이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
    }
}
