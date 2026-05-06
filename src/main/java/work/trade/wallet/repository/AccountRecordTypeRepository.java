package work.trade.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.wallet.domain.AccountRecordType;

public interface AccountRecordTypeRepository extends JpaRepository<AccountRecordType, String> {
}
