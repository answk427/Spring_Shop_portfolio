package work.trade.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import work.trade.auth.dto.request.LoginRequestDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
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
}