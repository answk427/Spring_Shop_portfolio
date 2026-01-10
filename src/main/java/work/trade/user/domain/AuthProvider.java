package work.trade.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "auth_providers")
public class AuthProvider {
    @Id
    @Column(length = 20)
    String code;

    @Column(length = 50)
    String name;

    @Column(columnDefinition="TEXT")
    String description;
}
