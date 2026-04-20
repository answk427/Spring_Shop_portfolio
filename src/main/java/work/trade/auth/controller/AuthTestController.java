package work.trade.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.AccessTokenResponseDto;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthTestController {

    private final JwtTokenUtil jwtTokenUtil;

    //permitAll 적용확인1
    @GetMapping("/auth/test")
    public String permitAllTest() {
        return "ok";
    }

    //permitAll 적용확인1
    @GetMapping("/notPermit")
    public String notPermit() {
        return "ok";
    }

    //토큰이 반환되는지 확인
    @PostMapping("/auth/test/login")
    public ResponseEntity<AccessTokenResponseDto> login(@RequestBody @Valid LoginRequestDto requestDto) {
        //실제로는 로그인한 ID의 PK를 넣어야함
        String accessToken = jwtTokenUtil.createAccessToken("1", List.of(Role.USER));
        String refreshToken = jwtTokenUtil.createRefreshToken("1");

        //RefreshToken HttpOnly 쿠키에 설정
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(jwtTokenUtil.getRefreshTokenExpiration())
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponseDto(
                        accessToken,
                        jwtTokenUtil.getAccessTokenExpiration()
                ));
    }

    //토큰을 실어서 보냈을 시 통과되는지 확인
    @GetMapping("/requestWithToken")
    public String authTest() {
        return "ok";
    }

    //관리자 권한 확인
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
