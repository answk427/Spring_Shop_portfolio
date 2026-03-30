package work.trade.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.user.domain.AuthProvider;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.exception.AuthProviderNotFoundException;
import work.trade.user.exception.UserNotFoundException;
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
            authProvider = apRepository.findById(authProviderId)
                    .orElseThrow(() -> new AuthProviderNotFoundException());
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
        user.updateFromDto(dto);
        user.updatePasswordHash(createPasswordHash(dto.getPassword()));
        return userMapper.toDto(user);
    }

    @Override
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
        userRepository.delete(user);
    }
}
