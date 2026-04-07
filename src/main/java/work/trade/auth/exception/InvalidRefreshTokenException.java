package work.trade.auth.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {

    private final static HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public InvalidRefreshTokenException() {
        super("유효하지 않은 RefreshToken입니다", STATUS);
    }

    public InvalidRefreshTokenException(String message) {
        super(message, STATUS);
    }
}
