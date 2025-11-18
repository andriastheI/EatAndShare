package edu.carroll.eatAndShare.backEnd.repo;

import edu.carroll.eatAndShare.backEnd.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Filename: CategoryRepository.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * Repository interface used for performing CRUD operations on Category
 * entities. Extends JpaRepository to provide built-in JPA operations
 * such as save, findById, findAll, and delete. Also includes custom
 * derived query methods for looking up categories by name.
 */

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Finds a category by its name, ignoring case sensitivity.
     *
     * @param categoryName the name of the category to search
     * @return an Optional containing the category if found, otherwise empty
     */
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    /**
     * Finds a category by its exact name (case-sensitive).
     *
     * @param categoryName the exact category name to search
     * @return an Optional containing the category if found, otherwise empty
     */
    Optional<Category> findByCategoryName(String categoryName);
}
