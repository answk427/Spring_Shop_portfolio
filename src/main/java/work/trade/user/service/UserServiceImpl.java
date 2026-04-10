package work.trade.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import work.trade.auth.exception.PasswordInvalidException;
import work.trade.auth.role.Role;
import work.trade.user.domain.AuthProvider;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.exception.AuthProviderNotFoundException;
import work.trade.user.exception.UserDuplicateEmailException;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.mapper.UserMapper;
import work.trade.user.repository.AuthProviderRepository;
import work.trade.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthProviderRepository apRepository;

    private final PasswordEncoder passwordEncoder;

    //Util----------------------------//
    private String createPasswordHash(String password) {
        return passwordEncoder.encode(password);
    }

//----------------------------//

    @Override
    public UserDto createUser(UserCreateRequestDto dto) {
        //중복 체크
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            throw new UserDuplicateEmailException();
        });

        String authProviderId = dto.getAuthProviderId();
        AuthProvider authProvider = null;
        if (authProviderId != null) {
            authProvider = apRepository.findById(authProviderId)
                    .orElseThrow(() -> new AuthProviderNotFoundException());
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(createPasswordHash(dto.getPassword()))
                .authProvider(authProvider)
                .role(Role.USER)
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findUser(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException());
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
        user.updateFromDto(dto);
        if (StringUtils.hasText(dto.getPassword())) {
            user.updatePasswordHash(createPasswordHash(dto.getPassword()));
        }
        return userMapper.toDto(user);
    }

    @Override
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
        userRepository.delete(user);
    }

    @Override
    public UserDto authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        //비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new PasswordInvalidException();
        }
        return userMapper.toDto(user);
    }


}
