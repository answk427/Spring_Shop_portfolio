package work.trade.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.wallet.domain.AccountRecord;

public interface AccountRecordRepository extends JpaRepository<AccountRecord, Long> {
}
