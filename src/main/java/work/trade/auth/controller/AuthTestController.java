package work.trade.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.jwt.JwtTokenUtil;
import work.trade.auth.role.Role;

import java.util.List;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class AuthTestController {

    private final JwtTokenUtil jwtTokenUtil;

    //permitAll 적용확인1
    @GetMapping("/auth/test")
    public String permitAllTest() {
        return "ok";
    }

    //permitAll 적용확인1
    @GetMapping("/notPermit")
    public String notPermit() {
        return "ok";
    }

    //토큰이 반환되는지 확인
    @PostMapping("/auth/test/login")
    public LoginResponseDto login(@RequestBody @Valid LoginRequestDto requestDto) {
        //실제로는 ID에 Email이 아닌 PK를 넣어야 함
        return new LoginResponseDto(jwtTokenUtil.createToken(requestDto.getId(), List.of(Role.USER)));
    }

    //토큰을 실어서 보냈을 시 통과되는지 확인
    @GetMapping("/requestWithToken")
    public String authTest() {
        return "ok";
    }

    //관리자 권한 확인
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
