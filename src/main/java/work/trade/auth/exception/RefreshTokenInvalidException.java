package work.trade.auth.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class RefreshTokenInvalidException extends BusinessException {

    private final static HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public RefreshTokenInvalidException() {
        super("유효하지 않은 RefreshToken입니다", STATUS);
    }

    public RefreshTokenInvalidException(String message) {
        super(message, STATUS);
    }
}
