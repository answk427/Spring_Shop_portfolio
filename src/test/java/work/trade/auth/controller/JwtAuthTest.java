package work.trade.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.exception.AuthErrorCode;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.service.AuthService;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class JwtAuthTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpw");


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthService authService;

//*******************************//

    @Test
    @DisplayName("로그인 시 JwtToken 반환 확인")
    void login() throws Exception {
        //given
        String mail = "test@test.com";
        String password = "asdf1234";
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto(mail, password, "user1", "LOCAL");
        UserDto userDto = userService.createUser(createRequestDto);


        //when, then
        LoginRequestDto loginRequestDto = new LoginRequestDto(mail, password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("유효하지 않은 이메일 형식 검증 되는지 확인")
    void validateEmail() throws Exception {
        LoginRequestDto request = new LoginRequestDto("invalid-email", "1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void CreateUserAndLogin() throws Exception {
        //given
        UserCreateRequestDto userCreateDto = getUserCreateDto();
        userService.createUser(userCreateDto);

        LoginRequestDto loginRequestDto = new LoginRequestDto(userCreateDto.getEmail(), userCreateDto.getPassword());

        //when, then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("아이디, 비밀번호 불일치")
    @Transactional
    void CreateUserAndLoginUnMatch() throws Exception {
        //given
        UserCreateRequestDto userCreateDto = getUserCreateDto();
        userService.createUser(userCreateDto);

        LoginRequestDto loginRequestDtoEmail = new LoginRequestDto("NotExistEmail@gmail.com", userCreateDto.getPassword());
        LoginRequestDto loginRequestDtoPassword = new LoginRequestDto(userCreateDto.getEmail(), "WrongPassword");

        //when, then
        //존재하지 않는 아이디 로그인
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDtoEmail)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        //잘못된 비밀번호 로그인
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDtoPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    private UserCreateRequestDto getUserCreateDto() {
        UserCreateRequestDto requestDto = new UserCreateRequestDto("test@test.com", "testPassword", "testName", null);
        return requestDto;
    }

//Refresh*******************************//
    @Test
    @DisplayName("RefreshToken 발급 확인")
    void getRefreshToken() throws Exception {
        //given
        UserCreateRequestDto createRequestDto = getUserCreateDto();
        UserDto userDto = userService.createUser(createRequestDto);

        LoginRequestDto loginRequestDto = new LoginRequestDto(createRequestDto.getEmail(), createRequestDto.getPassword());

        //when, then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                //Cookie 검증
                .andExpect(cookie().exists("refreshToken")) // 쿠키 이름 확인
                .andExpect(cookie().httpOnly("refreshToken", true)) // HttpOnly 설정 확인
                .andExpect(cookie().path("refreshToken", "/")) // 경로 확인
                .andExpect(cookie().maxAge("refreshToken", (int) jwtTokenUtil.getRefreshTokenExpiration())); // 설정한 시간 확인
    }

    @Test
    @DisplayName("만료된 토큰으로 접근 시 401 에러 발생")
    void expiredTokenTest() throws Exception {
        // given
        //임시로 token 유효기간을 0으로 설정
        ReflectionTestUtils.setField(jwtTokenUtil, "accessTokenExpiration", 0);

        UserCreateRequestDto createRequestDto = getUserCreateDto();
        userService.createUser(createRequestDto);
        LoginRequestDto loginRequestDto = new LoginRequestDto(createRequestDto.getEmail(), createRequestDto.getPassword());

        LoginResponseDto login = authService.login(loginRequestDto);
        String accessToken = login.accessToken();
        String refreshToken = login.refreshToken();

        // when & then
        //만료된 AccessToken으로 요청시 401반환
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(AuthErrorCode.EXPIRED_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(AuthErrorCode.EXPIRED_TOKEN.getMessage()));

        //token 유효기간 재설정
        ReflectionTestUtils.setField(jwtTokenUtil, "accessTokenExpiration", 36000);
        //AccessToken 재발급 요청
        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        String newAccessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

        //새로 발급받은 AccessToken으로 요청
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());
    }
}