package work.trade.auth.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "ID는 필수입니다.")
    @Email
    private String id;

    @NotBlank(message = "password는 필수입니다.")
    private String password;
}