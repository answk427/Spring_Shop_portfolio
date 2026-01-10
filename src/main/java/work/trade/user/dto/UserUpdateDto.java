package work.trade.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.user.domain.User;

import java.time.LocalDateTime;

//회원 정보 수정용 DTO
@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDto {

     @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
    private String name;

    public void updateEntity(User user) {
        if (email != null)      user.setEmail(email);
        if (password != null)   user.setPasswordHash(convertPassword(password));
        if (name != null)       user.setName(name);

        user.setUpdatedAt(LocalDateTime.now());
    }

    //아직 미구현
    private String convertPassword(String password) {
        return null;
    }
}
