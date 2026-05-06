package work.trade.wallet.domain;

import jakarta.persistence.*;
import lombok.*;
import work.trade.user.domain.User;
import work.trade.wallet.exception.WalletAmountException;
import work.trade.wallet.exception.WalletInsufficientBalanceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Builder
    private Wallet(@NonNull User user) {
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;


    //잔고 증가
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletAmountException("입금액은 0보다 커야 합니다");
        }
        balance = balance.add(amount);
    }

    // 출금
    public void deduct(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletAmountException("출금액은 0보다 커야 합니다");
        }

        //잔고 부족
        if (balance.compareTo(amount) < 0) {
            throw new WalletInsufficientBalanceException();
        }

        balance = balance.subtract(amount);
    }

    // 판매상품 환불로 인한 출금은 소지금액 음수 허용
    public void deductByRefund(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletAmountException("출금액은 0보다 커야 합니다");
        }

        balance = balance.subtract(amount);
    }

    //유저가 삭제됐을 경우 연관관계 해제
    public void setUserNull() {
        user = null;
    }
}
