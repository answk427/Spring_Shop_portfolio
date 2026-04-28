package work.trade.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import work.trade.cart.domain.Cart;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser_Id(Long userId);
    Optional<Cart> findByUser_IdAndProduct_Id(Long userId, Long productId);
    void deleteAllByUser_Id(Long userId);

    //한번의 쿼리로 모두 삭제
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Cart c where c.user.id = :userId")
    void deleteAllByUserIdInBatch(@Param("userId") Long userId);

    //===============JOIN FETCH FUNC==================//
    @Query("select c from Cart c " +
            "join fetch c.product p " +
            "join fetch p.category " +
            "join fetch p.seller " +
            "where c.user.id = :userId")
    List<Cart> findByUser_IdFetchJoin(@Param("userId") Long userId);

    @Query("select c from Cart c " +
            "join fetch c.product p " +
            "join fetch p.category " +
            "join fetch p.seller " +
            "where c.id = :id")
    Optional<Cart> findByIdFetchJoin(@Param("id") Long id);

}
