package work.trade.user.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import work.trade.auth.role.Role;

import java.time.LocalDateTime;

//기본 조회용 DTO
@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private AuthProviderDto authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
