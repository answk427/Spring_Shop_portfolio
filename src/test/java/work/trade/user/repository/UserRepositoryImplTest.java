package work.trade.user.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import work.trade.user.domain.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor
class UserRepositoryImplTest {

    @Autowired
    private UserRepositoryImpl repo;

    @Test
    @Transactional
    @Rollback(value = false)
    void save() {
        User user = new User();
        user.setName("testA");
        user.setEmail("answk426@naver.com");
        repo.save(user);
    }

    @Test
    void findById() {
    }

    @Test
    void update() {
    }

    @Test 
    void delete() {
    }
}