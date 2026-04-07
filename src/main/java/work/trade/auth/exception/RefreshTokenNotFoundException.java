package work.trade.auth.exception;

import org.springframework.http.HttpStatus;
import work.trade.common.exception.BusinessException;

public class RefreshTokenNotFoundException extends BusinessException {

    public RefreshTokenNotFoundException() {
        super("RefreshToken을 찾을 수 없습니다", HttpStatus.UNAUTHORIZED);
    }
}
