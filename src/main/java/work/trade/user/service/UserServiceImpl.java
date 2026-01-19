package work.trade.user.service;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.user.domain.AuthProvider;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.mapper.UserMapper;
import work.trade.user.repository.AuthProviderRepository;
import work.trade.user.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthProviderRepository apRepository;

//Util----------------------------//
    private String createPasswordHash(String password) {
        //todo : Hash 생성 후 반환
        return password;
    }
//----------------------------//

    @Override
    public UserDto createUser(UserCreateRequestDto dto) {
        String authProviderId = dto.getAuthProviderId();
        AuthProvider authProvider = null;
        if (authProviderId != null) {
            //커스텀예외로 추후 수정
            authProvider = apRepository.findById(authProviderId)
                    .orElseThrow(() -> new IllegalStateException("비정상적인 로그인방식"));
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(createPasswordHash(dto.getPassword()))
                .authProvider(authProvider)
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public Optional<UserDto> findUser(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        //커스텀예외로 추후 수정
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        user.updateFromDto(dto);
        user.updatePasswordHash(createPasswordHash(dto.getPassword()));
        return userMapper.toDto(user);
    }

    @Override
    public void deleteById(Long id) {
        //커스텀예외로 추후 수정
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}
