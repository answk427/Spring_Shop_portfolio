package work.trade.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.AccessTokenResponseDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.service.AuthService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDto> login(@RequestBody @Valid LoginRequestDto requestDto) {
        log.info("POST /api/auth/login start controller");

        LoginResponseDto response = authService.login(requestDto);

        //RefreshToken HttpOnly 쿠키에 설정
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(response.refreshTokenExpiresIn())
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponseDto(
                        response.accessToken(),
                        response.accessTokenExpiresIn()
                ));
    }

    //RefreshToken으로 AccessToken 갱신
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        log.info("POST /api/auth/refresh start controller");

        String refreshToken = extractRefreshTokenFromCookie(request);

        LoginResponseDto response = authService.refreshAccessToken(refreshToken);

        // 새로운 RefreshToken을 쿠키에 설정
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(response.refreshTokenExpiresIn())
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponseDto(
                        response.accessToken(),
                        response.accessTokenExpiresIn()
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        log.info("POST /api/auth/logout start controller");

        String refreshToken = extractRefreshTokenFromCookie(request);
        String accessToken = extractAccessTokenFromHeader(request);

        //Redis에서 RefreshToken 삭제, AccessToken BlackList 등록
        authService.logout(refreshToken, accessToken);

        //RefreshToken 쿠키 삭제
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "로그아웃 되었습니다"));
    }


//*************************************//

    //쿠키에서 RefreshToken 추출
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        throw new RuntimeException("RefreshToken 쿠키를 찾을 수 없습니다");
    }

    private String extractAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("AccessToken을 찾을 수 없습니다");
    }
}




