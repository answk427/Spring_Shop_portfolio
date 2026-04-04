package work.trade.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
