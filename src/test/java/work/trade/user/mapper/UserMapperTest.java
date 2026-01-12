package work.trade.user.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import work.trade.user.domain.AuthProvider;
import work.trade.user.domain.User;
import work.trade.user.dto.request.UserCreateRequestDto;
import work.trade.user.dto.request.UserUpdateDto;
import work.trade.user.dto.response.SellerDto;
import work.trade.user.dto.response.UserDto;
import work.trade.user.dto.response.UserSummaryDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper mapper;

    private User getTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("aaa@bbb.com");
        user.setName("홍길동");
        user.setPasswordHash("TempHashPassword");

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        AuthProvider provider = new AuthProvider();
        provider.setCode("google");
        provider.setName("Google");
        provider.setDescription("설명");
        user.setAuthProvider(provider);
        return user;
    }

    @Test
    void createToEntity() {
        // given
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setEmail("test@example.com");
        dto.setPassword("12345678");
        dto.setName("홍길동");

        // when
        User user = mapper.toEntity(dto);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getName()).isEqualTo("홍길동");
        // passwordHash는 매퍼에서 처리 안 하므로 null이어야 함
        assertThat(user.getPasswordHash()).isNull();

        assertThat(user.getId()).isNull();
        assertThat(user.getAuthProvider()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    void updateEntityFromDto() {
        // given
        User user = new User();
        user.setEmail("old@example.com");
        user.setPasswordHash("OLD_HASH");
        user.setName("Old User");

        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("new@example.com");
        dto.setName("새로운 이름");
        dto.setPassword("newpass!!!"); // passwordHash는 ignore되어야 함

        // when
        mapper.updateEntityFromDto(dto, user);

        // then
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getName()).isEqualTo("새로운 이름");
        // passwordHash는 ignore이므로 그대로 남아야 함
        assertThat(user.getPasswordHash()).isEqualTo("OLD_HASH");

        assertThat(user.getId()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getAuthProvider()).isNull();
    }

    @Test
    void userToDto() {
        // given
        User user = getTestUser();

        // when
        UserDto dto = mapper.toDto(user);

        // then
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(user.getUpdatedAt());

        assertThat(dto.getAuthProvider()).isNotNull();
        assertThat(dto.getAuthProvider().getName()).isEqualTo(user.getAuthProvider().getName());
        assertThat(dto.getAuthProvider().getCode()).isEqualTo(user.getAuthProvider().getCode());
        assertThat(dto.getAuthProvider().getDescription()).isEqualTo(user.getAuthProvider().getDescription());
    }

    @Test
    void toSummaryDto() {
        // given
        User user = getTestUser();

        // when
        UserSummaryDto dto = mapper.toSummaryDto(user);

        //then
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getName()).isEqualTo(user.getName());

        assertThat(dto.getAuthProvider()).isNotNull();
        assertThat(dto.getAuthProvider().getName()).isEqualTo(user.getAuthProvider().getName());
        assertThat(dto.getAuthProvider().getCode()).isEqualTo(user.getAuthProvider().getCode());
        assertThat(dto.getAuthProvider().getDescription()).isEqualTo(user.getAuthProvider().getDescription());
    }

    @Test
    void toSellerDto() {
        //given
        User user = getTestUser();

        //when
        SellerDto dto = mapper.toSellerDto(user);

        //then
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(user.getName());
    }
}