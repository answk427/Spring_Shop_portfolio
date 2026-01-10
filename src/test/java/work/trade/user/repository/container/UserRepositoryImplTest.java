package work.trade.user.repository.container;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.user.domain.User;
import work.trade.user.repository.UserRepositoryImpl;
import work.trade.user.service.UserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class UserRepositoryImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // 컨테이너 테스트용으로만 ddl-auto를 create로
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Test
    //Transactional을 붙이지 않는 이유
    //실제 db와 연결된 상태에서 commit 이후 데이터를 조회해서
    //실제로 commit 됐는지 검사
    void save() {
        User user = new User();
        user.setName("testA");
        user.setEmail("answk426@naver.com");
        userService.createUser(user);

        User findUser = userService.getUser(user.getId());
        assertThat(findUser.getId()).isEqualTo(user.getId());
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