package work.trade.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import work.trade.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory_Id(Long categoryId, Pageable pageable);
    Page<Product> findBySeller_Id(Long sellerId, Pageable pageable);
}
