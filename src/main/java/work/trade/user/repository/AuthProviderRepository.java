package work.trade.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.user.domain.AuthProvider;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, String> {
}