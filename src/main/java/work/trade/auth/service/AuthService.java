package work.trade.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.exception.RefreshTokenInvalidException;
import work.trade.user.dto.response.UserDto;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.user.service.UserService;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        log.info("Start Login. userEmail: {}", requestDto.getId());

        //로그인 요청시 ID = email
        UserDto user = userService.authenticate(requestDto.getId(), requestDto.getPassword());

        String accessToken = jwtTokenUtil.createAccessToken(user.getId().toString(), List.of(user.getRole()));
        String refreshToken = jwtTokenUtil.createRefreshToken(user.getId().toString());

        //RefreshToken Redis에 저장
        jwtTokenUtil.saveRefreshTokenToRedis(refreshToken, user.getId().toString());

        log.info("Login successful - userId: {}", user.getId());

        return new LoginResponseDto(accessToken,
                jwtTokenUtil.getAccessTokenExpiration(),
                refreshToken,
                jwtTokenUtil.getRefreshTokenExpiration()
        );
    }

    //RefreshToken으로 새로운 AccessToken 발급
    @Transactional
    public LoginResponseDto refreshAccessToken(String oldRefreshToken) {
        log.info("Refresh token attempt");

        //검증
        if (!jwtTokenUtil.isValidRefreshToken(oldRefreshToken)) {
            log.warn("Invalid refresh token");
            throw new RefreshTokenInvalidException("유효하지 않은 RefreshToken입니다");
        }

        String userId = jwtTokenUtil.getUserIdFromRefreshToken(oldRefreshToken);
        UserDto user = userService.findUser(Long.parseLong(userId));

        //새로운 AccessToken 생성
        String newAccessToken = jwtTokenUtil.createAccessToken(
                userId,
                List.of(user.getRole())
        );

        //새로운 RefreshToken 생성 (Token Rotation)
        String newRefreshToken = jwtTokenUtil.createRefreshToken(userId);

        //RefreshToken Rotation (이전 토큰 무효화 + 새 토큰 저장)
        jwtTokenUtil.rotateRefreshToken(oldRefreshToken, newRefreshToken, userId);

        log.info("Token refreshed successfully - userId: {}", userId);

        return new LoginResponseDto(newAccessToken,
                jwtTokenUtil.getAccessTokenExpiration(),
                newRefreshToken,
                jwtTokenUtil.getRefreshTokenExpiration()
        );
    }

    //로그아웃(RefreshToken Redis에서 삭제)
    public void logout(String refreshToken, String accessToken) {
        log.info("Logout");

        jwtTokenUtil.invalidateRefreshToken(refreshToken);
        jwtTokenUtil.blacklistAccessToken(accessToken);

        log.info("Logout successful");
    }

    public void logout(String refreshToken) {
        logout(refreshToken, null);
    }
}
