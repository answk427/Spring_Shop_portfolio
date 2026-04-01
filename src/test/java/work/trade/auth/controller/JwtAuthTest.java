package work.trade.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    private final UserService userService;

    @Autowired
    public JwtAuthTest(MockMvc mockMvc, ObjectMapper objectMapper, UserService userService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userService = userService;
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
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
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

        String token = objectMapper.readTree(response).get("token").asText();

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
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
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
                .andExpect(jsonPath("$.token").doesNotExist());

        //잘못된 비밀번호 로그인
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDtoPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    private UserCreateRequestDto getUserCreateDto() {
        UserCreateRequestDto requestDto = new UserCreateRequestDto();
        requestDto.setEmail("test@test.com");
        requestDto.setPassword("testPassword");
        requestDto.setName("testName");

        return requestDto;
    }
}