package work.trade.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import work.trade.user.domain.User;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    //===============JOIN FETCH FUNC==================//
    @Query("select u from User u " +
            "join fetch u.authProvider " +
            "where u.id = :id")
    Optional<User> findByIdFetchJoin(@Param("id") Long id);

    @Query("select u from User u " +
            "join fetch u.authProvider " +
            "where u.email = :email")
    Optional<User> findByEmailFetchJoin(@Param("email") String email);
}
