package work.trade.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import work.trade.common.exception.BusinessException;
import work.trade.common.exception.dto.ErrorResponseDto;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponseDto getErrorResponseDto(String message) {
        return new ErrorResponseDto(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handle(Exception e) {
        log.error("Error occurred: ", e);
        return ResponseEntity.internalServerError().body(
                getErrorResponseDto("서버 오류가 발생했습니다 :" + e.getMessage())
        );
    }

    //BusinessException
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handle(BusinessException e) {
        log.error("BusinessError occurred: ", e);
        return ResponseEntity.status(e.getStatus()).body(getErrorResponseDto(e.getMessage()));
    }

    //@Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handle(MethodArgumentNotValidException e) {
        log.warn("Valid Exception : ", e);
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(getErrorResponseDto(message));
    }

    //필터 지나고 컨트롤러에서 권한 막힐 경우
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handle(AccessDeniedException e) {
        log.warn("Authentication Exception: ", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).
                body(getErrorResponseDto(e.getMessage()));
    }
}

