package work.trade.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_providers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthProvider {

    @Builder
    private AuthProvider(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    @Id
    @Column(length = 20)
    String code;

    @Column(length = 50)
    String name;

    @Column(columnDefinition="TEXT")
    String description;
}


