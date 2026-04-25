package work.trade.product.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import work.trade.product.domain.Product;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    Page<Product> findByCategory_Id(Long categoryId, Pageable pageable);
    Page<Product> findBySeller_Id(Long sellerId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @QueryHints({@QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "BYPASS")})
    Optional<Product> findByIdWithLock(@Param("id") Long id);

//===============JOIN FETCH FUNC==================//
    @Query("select p from Product p " +
            "join fetch p.seller " +
            "join fetch p.category " +
            "where p.id = :id")
    Optional<Product> findByIdFetchJoin(@Param("id") Long id);
}
