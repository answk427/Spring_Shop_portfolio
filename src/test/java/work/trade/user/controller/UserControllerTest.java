package work.trade.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.exception.InvalidPasswordException;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;
import work.trade.auth.service.AuthService;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.exception.UserNotFoundException;
import work.trade.user.repository.UserRepository;
import work.trade.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class UserControllerTest {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenUtil jwtTokenUtil;

//*********************//
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthService authService;


    //*********************//
    private Long testUserId;
    private String testUserToken;
    private String testUserEmail = "Init@email.com";
    private String testUserPassword = "init4567";
    private String testUserName = "InitTestName";

    @BeforeEach
    void Init() {
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setEmail(testUserEmail);
        createRequestDto.setPassword(testUserPassword);
        createRequestDto.setName(testUserName);
        UserDto userDto = userService.createUser(createRequestDto);
        testUserId = userDto.getId();

        //테스트용 토큰 생성
        testUserToken = jwtTokenUtil.createToken(userDto.getId().toString(), List.of(Role.USER));
    }

//*********************//

    @Test
    void createUser() throws Exception {
        //given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setEmail("test@email.com");
        createRequestDto.setPassword("asdf4567");
        createRequestDto.setName("testName");

        //when, then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(jsonPath("$.email").value(createRequestDto.getEmail()))
                .andExpect(jsonPath("$.name").value(createRequestDto.getName()))
                .andExpect(jsonPath("$.id").isNotEmpty());

        UserDto userDto = userService.findByEmail(createRequestDto.getEmail());
        assertThat(userDto).isNotNull();
    }

    @Test
    void getMyProfile() throws Exception {
        //given
        //init에서 생성한 유저, 토큰

        //when, then
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(jsonPath("$.email").value(testUserEmail))
                .andExpect(jsonPath("$.name").value(testUserName))
                .andExpect(jsonPath("$.id").value(testUserId));
    }

    @Test
    void getUser() throws Exception {
        //given
        //init에서 생성한 유저, 토큰

        //when, then
        mockMvc.perform(get("/users/" + testUserId)
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(jsonPath("$.email").value(testUserEmail))
                .andExpect(jsonPath("$.name").value(testUserName))
                .andExpect(jsonPath("$.id").value(testUserId));
    }

    @Test
    void getUserByEmail() throws Exception {
        //given
        //init에서 생성한 유저, 토큰

        //when, then
        mockMvc.perform(get("/users/email")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("email", testUserEmail))
                .andExpect(jsonPath("$.email").value(testUserEmail))
                .andExpect(jsonPath("$.name").value(testUserName))
                .andExpect(jsonPath("$.id").value(testUserId));
    }

    @Test
    void updateUser() throws Exception {
        //given
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("updateEmail@email.com");
        updateDto.setPassword("update1234");
        updateDto.setName("updateName");

        //when
        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(jsonPath("$.email").value(updateDto.getEmail()))
                .andExpect(jsonPath("$.name").value(updateDto.getName()));

        //then
        UserDto userDto = userService.findUser(testUserId);
        assertThat(userDto.getEmail()).isEqualTo(updateDto.getEmail());
        assertThat(userDto.getName()).isEqualTo(updateDto.getName());

        //변경 전 ID 로그인
        assertThatThrownBy(() -> authService
                .login(new LoginRequestDto(testUserEmail, testUserPassword)))
                .isInstanceOf(UserNotFoundException.class);

        //변경 전 패스워드로 로그인
        assertThatThrownBy(() -> authService
                .login(new LoginRequestDto(updateDto.getEmail(), testUserPassword)))
                .isInstanceOf(InvalidPasswordException.class);

        //변경 후 패스워드로 로그인
        assertThatCode(() -> authService
                .login(new LoginRequestDto(updateDto.getEmail(), updateDto.getPassword())))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteUser() throws Exception {
        //given
        //init에서 생성한 유저, 토큰

        //when
        mockMvc.perform(delete("/users/me")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        //then
        //삭제 된 아이디로 로그인
        assertThatThrownBy(() -> authService
                .login(new LoginRequestDto(testUserEmail, testUserPassword)))
                .isInstanceOf(UserNotFoundException.class);
    }
}