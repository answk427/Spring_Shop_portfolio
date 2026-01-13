package work.trade.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
