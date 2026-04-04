package work.trade.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    public ForbiddenException() {
        super("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
}
