package work.trade.auth.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import work.trade.auth.exception.AuthErrorCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Object exception = request.getAttribute("exception");
        AuthErrorCode errorCode = AuthErrorCode.UNAUTHORIZED;
        if (exception instanceof AuthErrorCode) {
            errorCode = (AuthErrorCode) exception;
        }

        log.error("인증 실패: {}", errorCode.getMessage());

        sendErrorResponse(response, errorCode);
    }

    private void sendErrorResponse(HttpServletResponse response, AuthErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        //Map으로 JSON 구조 생성
        Map<String, Object> body = new HashMap<>();
        body.put("code", errorCode.getCode());
        body.put("message", errorCode.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
