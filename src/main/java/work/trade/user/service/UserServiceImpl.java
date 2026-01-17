package work.trade.user.service;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.mapper.UserMapper;
import work.trade.user.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

//Util----------------------------//
    private String createPasswordHash(String password) {
        //todo : Hash 생성 후 반환
        return password;
    }
//----------------------------//

    @Override
    public UserDto createUser(User user) {
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public Optional<UserDto> findUser(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User updatedUser = userRepository.update(id, dto);
        updatedUser.setPasswordHash(createPasswordHash(dto.getPassword()));
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.delete(id);
    }
}
