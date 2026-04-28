package work.trade.user.dto.response;

import work.trade.auth.role.Role;

import java.time.LocalDateTime;

//기본 조회용 DTO
public record UserDto(
        Long id,
        String email,
        String name,
        Role role,
        AuthProviderDto authProvider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
