package work.trade.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import work.trade.auth.dto.request.LoginRequestDto;
import work.trade.auth.dto.response.LoginResponseDto;
import work.trade.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody @Valid LoginRequestDto requestDto) {
        return authService.login(requestDto);
    }

}
 