package work.trade.wallet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.wallet.domain.AccountRecord;

public interface AccountRecordRepository extends JpaRepository<AccountRecord, Long> {
    Page<AccountRecord> findByWallet_Id(Long walletId, Pageable pageable);
}
