package work.trade.user.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import work.trade.user.domain.AuthProvider;
import work.trade.user.domain.User;
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

//*******************************//

    private final Long tempUserId = 1L;

    //테스트용 유저 객체 생성
    private User getTestUser() {
        AuthProvider provider = AuthProvider.builder()
                .code("google")
                .name("Google")
                .description("설명")
                .build();

        User user = User.builder()
                .email("aaa@bbb.com")
                .name("홍길동")
                .passwordHash("TempHashPassword")
                .authProvider(provider)
                .build();

        ReflectionTestUtils.setField(user, "id", tempUserId);
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(user, "createdAt", now);
        ReflectionTestUtils.setField(user, "updatedAt", now);

        return user;
    }

//*******************************//

    @Test
    void updateEntityFromDto() {
        // given
        User user = getTestUser();
        String oldHash = user.getPasswordHash();

        UserUpdateDto dto = new UserUpdateDto("now@example.com", "newpass!!!", "새로운 이름");

        // when
        user.updateFromDto(dto);

        // then
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
        assertThat(user.getName()).isEqualTo(dto.getName());
        // passwordHash는 ignore이므로 그대로 남아야 함
        assertThat(user.getPasswordHash()).isEqualTo(oldHash);

        assertThat(user.getId()).isEqualTo(tempUserId);
    }

    @Test
    void userToDto() {
        // given
        User user = getTestUser();

        // when
        UserDto dto = mapper.toDto(user);

        // then
        assertThat(dto.id()).isEqualTo(user.getId());
        assertThat(dto.email()).isEqualTo(user.getEmail());
        assertThat(dto.name()).isEqualTo(user.getName());
        assertThat(dto.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(dto.updatedAt()).isEqualTo(user.getUpdatedAt());

        assertThat(dto.authProvider()).isNotNull();
        assertThat(dto.authProvider().name()).isEqualTo(user.getAuthProvider().getName());
        assertThat(dto.authProvider().code()).isEqualTo(user.getAuthProvider().getCode());
        assertThat(dto.authProvider().description()).isEqualTo(user.getAuthProvider().getDescription());
    }

    @Test
    void toSummaryDto() {
        // given
        User user = getTestUser();

        // when
        UserSummaryDto summaryDto = mapper.toSummaryDto(user);

        UserDto dto = mapper.toDto(user);
        UserSummaryDto dtoToSummaryDto = mapper.toSummaryDto(dto);

        //then
        assertThat(summaryDto.id()).isEqualTo(user.getId());
        assertThat(summaryDto.email()).isEqualTo(user.getEmail());
        assertThat(summaryDto.name()).isEqualTo(user.getName());

        assertThat(dtoToSummaryDto.id()).isEqualTo(dto.id());
        assertThat(dtoToSummaryDto.email()).isEqualTo(dto.email());
        assertThat(dtoToSummaryDto.name()).isEqualTo(dto.name());
    }

    @Test
    void toSellerDto() {
        //given
        User user = getTestUser();

        //when
        SellerDto dto = mapper.toSellerDto(user);

        //then
        assertThat(dto.email()).isEqualTo(user.getEmail());
        assertThat(dto.id()).isEqualTo(user.getId());
        assertThat(dto.name()).isEqualTo(user.getName());
    }
}