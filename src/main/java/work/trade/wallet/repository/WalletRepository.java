package work.trade.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.wallet.domain.Wallet;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser_Id(Long userId);

}
