package work.trade.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.mapper.UserMapper;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository{

    @PersistenceContext
    private EntityManager em;

    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        em.persist(user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        User findUser = em.find(User.class, id);
        return Optional.ofNullable(findUser);
    }

    @Override
    public void delete(User user) {
        em.remove(user);
    }
}
