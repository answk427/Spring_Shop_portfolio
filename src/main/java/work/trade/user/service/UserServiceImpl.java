package work.trade.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.trade.user.domain.User;
import work.trade.user.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User getUser(Long id) {
        Optional<User> findUser = userRepository.findById(id);
        return findUser.get();
    }

    @Override
    @Transactional
    public void createUser(User user) {
        userRepository.save(user);
    }
}
