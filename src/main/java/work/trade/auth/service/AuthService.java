package work.trade.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.user.exception.UserNotFoundException;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;
import work.trade.user.domain.User;
import work.trade.user.repository.UserRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto requestDto) {
        //로그인 요청시 ID = email
        User user = userRepository.findByEmail(requestDto.getId())
                .orElseThrow(() -> new UserNotFoundException());

//        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
//            throw new InvalidPasswordException();
//        }

        //추후 수정
        //User에 권한 컬럼 추가하고 가져오기
        String token = jwtTokenUtil.createToken(user.getId().toString(), List.of(Role.USER));
        return new LoginResponseDto(token);
    }
}
