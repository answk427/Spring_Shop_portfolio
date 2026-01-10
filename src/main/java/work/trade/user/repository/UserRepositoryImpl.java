package work.trade.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import work.trade.user.domain.User;
import work.trade.user.dto.UserUpdateDto;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository{

    @PersistenceContext
    private final EntityManager em;

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
    public void update(Long userId, UserUpdateDto updateDto) {
        User user = em.find(User.class, userId); 
        updateDto.updateEntity(user);
    }

    @Override
    public void delete(Long id) {
         User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
    }
}
