package work.trade.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.service.AuthService;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.service.UserService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtAuthTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthService authService;

    @Autowired
    public JwtAuthTest(MockMvc mockMvc, ObjectMapper objectMapper, UserService userService, JwtTokenUtil jwtTokenUtil, AuthService authService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authService = authService;
    }

    //토큰 없이 허용된 접근 검증
    @Test
    void permitAll() throws Exception {
        mockMvc.perform(get("/auth/test"))
                .andExpect(status().isOk());
    }

    //토큰 없으면 401(인증X)
    @Test
    void notPermit() throws Exception {
        mockMvc.perform(get("/notPermit"))
                .andExpect(status().isUnauthorized());
    }

    //로그인 토큰 반환
    @Test
    void login() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@test.com", "1234");

        mockMvc.perform(post("/auth/test/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    // 4. 유효하지 않은 이메일 형식 - 400
    @Test
    void validateEmail() throws Exception {
        LoginRequestDto request = new LoginRequestDto("invalid-email", "1234");

        mockMvc.perform(post("/auth/test/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    //토큰 실어서 요청 - 통과
    @Test
    void requestWithToken() throws Exception {
        //given
        LoginRequestDto request = new LoginRequestDto("test@test.com", "1234");

        String response = mockMvc.perform(post("/auth/test/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();

        //when,then
        mockMvc.perform(get("/requestWithToken")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    //DB에 유저 만들고 로그인
    @Test
    @Transactional
    void CreateUserAndLogin() throws Exception {
        //given
        UserCreateRequestDto userCreateDto = getUserCreateDto();
        userService.createUser(userCreateDto);

        LoginRequestDto loginRequestDto = new LoginRequestDto(userCreateDto.getEmail(), userCreateDto.getPassword());

        //when, then
        mockMvc.perform(post("/auth/login")
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
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDtoEmail)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        //잘못된 비밀번호 로그인
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDtoPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    private UserCreateRequestDto getUserCreateDto() {
        UserCreateRequestDto requestDto = new UserCreateRequestDto();
        requestDto.setEmail("test@test.com");
        requestDto.setPassword("testPassword");
        requestDto.setName("testName");

        return requestDto;
    }

//Refresh*************************//
    @Test
    @DisplayName("RefreshToken 발급 확인")
    void getRefreshToken() throws Exception {
        //given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setName("tester2");
        createRequestDto.setEmail("test2@test.com");
        createRequestDto.setPassword("zxcv1234");
        userService.createUser(createRequestDto);

        LoginRequestDto loginRequestDto = new LoginRequestDto("test@test.com", "zxcv1234");

        //when, then
        mockMvc.perform(post("/auth/login")
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

        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setName("tester2");
        createRequestDto.setEmail("test2@test.com");
        createRequestDto.setPassword("zxcv1234");
        userService.createUser(createRequestDto);
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@test.com", "zxcv1234");

        LoginResponseDto login = authService.login(loginRequestDto);
        String accessToken = login.accessToken();
        String refreshToken = login.refreshToken();

        // when & then
        //만료된 AccessToken으로 요청시 401반환
        mockMvc.perform(get("/requestWithToken")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        //token 유효기간 재설정
        ReflectionTestUtils.setField(jwtTokenUtil, "accessTokenExpiration", 36000);
        //AccessToken 재발급 요청
        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        String newAccessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

        //새로 발급받은 AccessToken으로 요청
        mockMvc.perform(get("/requestWithToken")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃")
    void logout() {

    }
}