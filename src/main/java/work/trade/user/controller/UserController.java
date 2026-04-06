package work.trade.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.dto.response.UserSummaryDto;
import work.trade.user.mapper.UserMapper;
import work.trade.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

//**********************//

    //회원 가입
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequestDto dto) {
        UserDto userDto = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    //본인 회원 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        UserDto userDto = userService.findUser(userId);
        return ResponseEntity.ok(userDto);
    }

    //특정 회원 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDto> getUser(@PathVariable("id") Long id) {
        UserDto userDto = userService.findUser(id);
        return ResponseEntity.ok(userMapper.toSummaryDto(userDto));
    }

    //이메일로 회원 조회
    @GetMapping("/email")
    public ResponseEntity<UserSummaryDto> getUserByEmail(@RequestParam("email") String email) {
        UserDto userDto = userService.findByEmail(email);
        return ResponseEntity.ok(userMapper.toSummaryDto(userDto));
    }

    //회원 정보 수정
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateUser(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        UserDto updatedUser = userService.updateUser(userId, dto);
        return ResponseEntity.ok(updatedUser);
    }

    //회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
