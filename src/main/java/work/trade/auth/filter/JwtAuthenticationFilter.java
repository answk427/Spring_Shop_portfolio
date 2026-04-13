package work.trade.auth.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import work.trade.auth.exception.AuthErrorCode;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, AuthenticationException {
        String token = extractTokenFromRequest(request);

        // 유효한 토큰이 존재하는 경우
        try {
            if (token != null && jwtTokenUtil.isValidAccessToken(token)) {
                //토큰에서 사용자 ID 추출
                String userId = jwtTokenUtil.getUserIdFromToken(token);

                //권한 추출
                List<GrantedAuthority> authorities = getAuthority(token);

                // Spring Security Context에 인증 정보 설정
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료됨");
            request.setAttribute("exception", AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            log.warn("토큰 검증 실패 : {}", e.getMessage());
            request.setAttribute("exception", AuthErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            log.warn("토큰 인증 과정 에러 발생 : {}", e.getMessage());
            request.setAttribute("exception", e.getMessage());
        }

        // 토큰이 없으면 그냥 진행 (공개 API나 로그인 페이지용)
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 토큰 추출
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거
        }
        return null;
    }

    // 유저의 권한 얻어오기
    private List<GrantedAuthority> getAuthority(String token) {
        List<String> roles = jwtTokenUtil.getRolesFromToken(token);

        return roles.stream()
                .map(Role::valueOf)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getAuthority()))
                .toList();
    }
}
