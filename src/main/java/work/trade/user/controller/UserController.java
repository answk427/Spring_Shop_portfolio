package work.trade.user.controller;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import work.trade.user.dto.UserCreateRequestDto;
import work.trade.user.dto.UserDto;

@RestController
@RequestMapping("/test")
public class UserController {

    @GetMapping("/modelattr")
    String attrTest(@ModelAttribute UserDto dto, BindingResult bindingResult) {
        return "ModelAttribute사용 시 잘못된 쿼리값 줬을 때";
    }

    @GetMapping("/queryparam")
    String queryparamTest(@RequestParam String name, @RequestParam int age) {
        return "RequestParam 테스트";
    }

    @GetMapping("/pathvar/{id}")
    String pathVariableTest(@PathVariable Long id) {
 return "Pathvar 테스트";
    }

    @GetMapping("/valid")
    String validTest(@Validated @ModelAttribute UserCreateRequestDto dto, BindingResult bindingResult){//, BindingResult bindingResult) {
        String str = "";
        str += " email : " + dto.getEmail();
        str += " password : " + dto.getPassword();
        str += " name : " + dto.getName();
        return str;
    }

}
