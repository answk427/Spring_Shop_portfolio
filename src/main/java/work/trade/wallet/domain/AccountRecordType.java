package work.trade.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_record_type")
@Getter
@NoArgsConstructor
public class AccountRecordType {

    @Builder
    private AccountRecordType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    @Id
    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
