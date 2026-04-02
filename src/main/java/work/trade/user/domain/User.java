package work.trade.user.domain;

import jakarta.persistence.*;
import lombok.*;
import work.trade.auth.role.Role;
import work.trade.user.dto.request.UserUpdateDto;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Builder
    public User(String email, String passwordHash, AuthProvider authProvider, String name, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.authProvider = authProvider;
        this.name = name;
        this.role = role;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_provider")
    private AuthProvider authProvider;

    @Column(length = 100)
    private String name;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    //레코드 생성/업데이트 시 자동갱신
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;


    public void updateFromDto(UserUpdateDto dto) {
        //password는 서비스에서 Hash로 변환 후 변경
        name = dto.getName();
        email = dto.getEmail();
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
