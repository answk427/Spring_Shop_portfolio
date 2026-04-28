package work.trade.user.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.user.domain.AuthProvider;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.AuthProviderDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.repository.AuthProviderRepository;
import work.trade.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserServiceImplTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

    @Autowired
    private EntityManager em;

    //------------
    @Autowired
    AuthProviderRepository apRepo;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    //------------
    @Autowired
    private PasswordEncoder passwordEncoder;

//------------------------------------------------------------------//

    private static final String testUserName = "testUserName";
    private static final String testUserEmail = "test@naver.com";
    private static final String testPassword = "testPasswordHash";
    private Long testUserId = -1L;

    private final String testApCode = "LOCAL";
    private String testApName = null;
    private String testApDesc = null;

    @BeforeEach
    @Transactional
    void InitData() {
        testUserId = createTestUser(testUserName, testUserEmail, testPassword).getId();

        AuthProvider authProvider = apRepo.getReferenceById(testApCode);
        testApName = authProvider.getName();
        testApDesc = authProvider.getDescription();
    }

    @Transactional
    UserDto createTestUser(String name, String email, String password) {
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto(email, password, name, testApCode);
        return userService.createUser(createRequestDto);
    }

    private void checkAuthProvider(AuthProviderDto findAuthProvider) {
        assertThat(findAuthProvider.getCode()).isEqualTo(testApCode);
        assertThat(findAuthProvider.getName()).isEqualTo(testApName);
        assertThat(findAuthProvider.getDescription()).isEqualTo(testApDesc);
    }

    private void checkUserDto(UserDto userDto, Long userId, String userName, String userEmail) {
        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isEqualTo(userId);
        assertThat(userDto.getName()).isEqualTo(userName);
        assertThat(userDto.getEmail()).isEqualTo(userEmail);
        assertThat(userDto.getCreatedAt()).isNotNull();
        assertThat(userDto.getUpdatedAt()).isNotNull();
    }
//------------------------------------------------------------------//

    @Test
    @Transactional
    void createUser() {
        //given
        String userName = "createUserName";
        String userEmail = "createTest@naver.com";
        String userPassword = "createTestPasswordHash";

        //when
        System.out.println("================= [로직 시작] =================");
        UserDto createUserDto = createTestUser(userName, userEmail, userPassword);
        System.out.println("================= [로직 종료] =================");

        //then
        //Repository에서 얻은 Entity 검증
        Optional<User> findUserOpt = userRepository.findById(createUserDto.getId());
        assertThat(findUserOpt.isPresent()).isTrue();
        User user = findUserOpt.get();

        assertThat(user.getId()).isEqualTo(createUserDto.getId());
        assertThat(user.getName()).isEqualTo(userName);
        assertThat(user.getEmail()).isEqualTo(userEmail);
        assertThat(passwordEncoder.matches(userPassword, user.getPasswordHash())).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();

        AuthProvider authProvider = user.getAuthProvider();
        assertThat(authProvider).isNotNull();
        assertThat(authProvider.getCode()).isEqualTo(testApCode);
        assertThat(authProvider.getName()).isEqualTo(testApName);
        assertThat(authProvider.getDescription()).isEqualTo(testApDesc);
    }

    @Test
    @Transactional
    void findUser() {
        //given
        //@BeforeEach에서 데이터 삽입

        //N+1 확인 위해 영속성 컨텍스트 초기화
        em.clear();

        //when
        System.out.println("================= [로직 시작] =================");
        UserDto userDto = userService.findUser(testUserId);
        System.out.println("================= [로직 종료] =================");

        //then
        checkUserDto(userDto, testUserId, testUserName, testUserEmail);

        if (userDto.getAuthProvider() != null) {
            AuthProviderDto findAuthProvider = userDto.getAuthProvider();
            checkAuthProvider(findAuthProvider);
        }

        //[로직시작] [로직종료] 사이에 SQL로그 1번 나가는지 확인
    }

    @Test
    @Transactional
    void updateUser() {
        //given
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Update Name");
        updateDto.setEmail("Update Email");
        updateDto.setPassword("Update Password");

        User oldUser = userRepository.findById(testUserId).get();
        String oldPasswordHash = oldUser.getPasswordHash();
        LocalDateTime oldCreatedAt = oldUser.getCreatedAt();
        LocalDateTime oldUpdatedAt = oldUser.getUpdatedAt();

        em.clear();

        //when
        System.out.println("================= [로직 시작] =================");
        userService.updateUser(testUserId, updateDto);
        System.out.println("================= [로직 종료] =================");

        Optional<User> userByRepoOpt = userRepository.findById(testUserId);

        //then
        assertThat(userByRepoOpt.isPresent()).isTrue();
        User updatedUser = userByRepoOpt.get();

        assertThat(updatedUser.getId()).isEqualTo(testUserId);
        assertThat(updatedUser.getName()).isEqualTo(updateDto.getName());
        assertThat(updatedUser.getEmail()).isEqualTo(updateDto.getEmail());
        assertThat(updatedUser.getPasswordHash()).isNotEqualTo(oldPasswordHash);

        // 업데이트 이전 시간보다 이후여야 함
        assertThat(updatedUser.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
        // 생성 시간은 변경되지 않아야 함
        assertThat(updatedUser.getCreatedAt()).isCloseTo(oldCreatedAt, within(1, ChronoUnit.MILLIS));

        //없는 id 변경 시도시 예외 반환
        assertThatThrownBy(() -> userService.updateUser(1818L, updateDto));

        //[로직시작] [로직종료] 사이에 SQL로그 1번 나가는지 확인
    }

    @Test
    @Transactional
    void deleteById() {
        //given
        //BeforeEach에서 데이터 삽입

        em.clear();

        //when
        System.out.println("================= [로직 시작] =================");
        userService.deleteById(testUserId);
        System.out.println("================= [로직 종료] =================");

        //then
        assertThatThrownBy(() -> userService.findUser(testUserId)).isInstanceOf(UserNotFoundException.class);

        //[로직시작] [로직종료] 사이에 SQL로그 1번 나가는지 확인
    }

    @Test
    @Transactional
    void findByEmail() {
        //given
        //@BeforeEach에서 데이터 삽입

        //N+1 확인 위해 영속성 컨텍스트 초기화
        em.clear();

        //when
        System.out.println("================= [로직 시작] =================");
        UserDto userDto = userService.findByEmail(testUserEmail);
        System.out.println("================= [로직 종료] =================");

        //then
        checkUserDto(userDto, testUserId, testUserName, testUserEmail);

        if (userDto.getAuthProvider() != null) {
            AuthProviderDto findAuthProvider = userDto.getAuthProvider();
            checkAuthProvider(findAuthProvider);
        }

        //[로직시작] [로직종료] 사이에 SQL로그 1번 나가는지 확인
    }


}