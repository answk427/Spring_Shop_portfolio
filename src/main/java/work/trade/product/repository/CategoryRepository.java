package work.trade.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.product.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
