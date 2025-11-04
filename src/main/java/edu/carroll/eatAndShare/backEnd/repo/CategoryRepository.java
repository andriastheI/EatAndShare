package edu.carroll.eatAndShare.backEnd.repo;

import edu.carroll.eatAndShare.backEnd.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link Category} entities.
 *
 * <p>This interface extends {@link JpaRepository}, giving it access to
 * all standard JPA operations such as:
 * <ul>
 *   <li>{@code save()} – insert or update a category</li>
 *   <li>{@code findById()} – retrieve a category by its ID</li>
 *   <li>{@code findAll()} – retrieve all categories</li>
 *   <li>{@code deleteById()} – remove a category</li>
 * </ul>
 * </p>
 *
 * <p>Additionally, this repository defines custom query methods based on
 * Spring Data JPA’s derived query syntax to fetch categories by name.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     Optional&lt;Category&gt; cat = categoryRepository.findByCategoryNameIgnoreCase("Dessert");
 * </pre>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Finds a category by its name, ignoring case sensitivity.
     *
     * @param categoryName the name of the category to look for
     * @return an {@link Optional} containing the category if found, or empty if not found
     */
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    /**
     * Finds a category by its exact name (case-sensitive match).
     *
     * @param categoryName the name of the category to look for
     * @return an {@link Optional} containing the category if found, or empty if not found
     */
    Optional<Category> findByCategoryName(String categoryName);
}
