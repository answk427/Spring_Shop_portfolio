package work.trade.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import work.trade.product.domain.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, parent_id
            FROM categories
            WHERE id = :categoryId

            UNION ALL

            SELECT c.id, c.parent_id
            FROM categories c
            JOIN category_tree ct
                ON c.parent_id = ct.id
        )
        SELECT id
        FROM category_tree
        """, nativeQuery = true)
    List<Long> findAllChildCategoryIds(Long categoryId);
}
