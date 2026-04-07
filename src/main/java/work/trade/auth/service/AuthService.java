package work.trade.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.exception.InvalidPasswordException;
import work.trade.auth.exception.InvalidRefreshTokenException;
import work.trade.user.exception.UserNotFoundException;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.user.domain.User;
import work.trade.user.repository.UserRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        //로그인 요청시 ID = email
        User user = userRepository.findByEmail(requestDto.getId())
                .orElseThrow(() -> {
                    log.warn("User not found - email: {}", requestDto.getId());
                    return new UserNotFoundException();
                });


        //비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password - email: {}", requestDto.getId());
            throw new InvalidPasswordException();
        }

        //AccessToken 생성
        String accessToken = jwtTokenUtil.createAccessToken(user.getId().toString(), List.of(user.getRole()));

        //RefreshToken 생성
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
            throw new InvalidRefreshTokenException("유효하지 않은 RefreshToken입니다");
        }

        //RefreshToken에서 사용자 ID 추출
        String userId = jwtTokenUtil.getUserIdFromRefreshToken(oldRefreshToken);

        //사용자 조회 (권한 정보 필요)
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> {
                    log.warn("User not found - userId: {}", userId);
                    return new UserNotFoundException();
                });

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
    public void logout(String refreshToken) {
        log.info("Logout");

        jwtTokenUtil.invalidateRefreshToken(refreshToken);

        log.info("Logout successful");
    }
}
