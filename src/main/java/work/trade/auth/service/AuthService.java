package work.trade.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.exception.InvalidPasswordException;
import work.trade.user.exception.UserNotFoundException;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.user.domain.User;
import work.trade.user.repository.UserRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto requestDto) {
        //로그인 요청시 ID = email
        User user = userRepository.findByEmail(requestDto.getId())
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        //추후 수정
        //User에 권한 컬럼 추가하고 가져오기
        String token = jwtTokenUtil.createToken(user.getId().toString(), List.of(user.getRole()));
        return new LoginResponseDto(token);
    }
}
