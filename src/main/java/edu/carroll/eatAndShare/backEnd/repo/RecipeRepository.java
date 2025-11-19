package edu.carroll.eatAndShare.backEnd.repo;

import edu.carroll.eatAndShare.backEnd.model.Recipe;
import edu.carroll.eatAndShare.backEnd.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Filename: RecipeRepository.java
 * Author: Andrias and Selin
 * Date: October 11, 2025
 *
 * Description:
 * Repository interface for managing Recipe entities. Extends JpaRepository
 * to provide built-in CRUD operations and supports custom query methods for
 * advanced searching, sorting, and pagination of recipes.
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

    /**
     * Retrieves all recipes ordered by ID in descending order.
     * Typically used to display the newest recipes first.
     *
     * @return a list of recipes sorted by ID (newest first)
     */
    List<Recipe> findAllByOrderByIdDesc();

    /**
     * Retrieves all recipes matching a specific category name (case-insensitive).
     *
     * @param categoryName the category name to search for
     * @return a list of recipes belonging to the category
     */
    List<Recipe> findByCategory_CategoryNameIgnoreCase(String categoryName);

    /**
     * Retrieves a paginated list of all recipes sorted by ID in descending order.
     *
     * @param pageable pagination information
     * @return a page of recipes sorted by ID (newest first)
     */
    Page<Recipe> findAllByOrderByIdDesc(Pageable pageable);

    /**
     * Performs a full-text style search across title, instructions,
     * category, and ingredient names. Supports pagination.
     *
     * @param q the search query text
     * @param pageable pagination information
     * @return a page of recipes matching the search criteria
     */
    @Query("""
        select distinct r from Recipe r
        left join r.category c
        left join r.recipeIngredients ri
        left join ri.ingredient i
        where lower(r.title) like lower(concat('%', :q, '%'))
           or lower(r.instructions) like lower(concat('%', :q, '%'))
           or lower(c.categoryName) like lower(concat('%', :q, '%'))
           or lower(i.ingredientName) like lower(concat('%', :q, '%'))
    """)
    Page<Recipe> search(@Param("q") String q, Pageable pageable);

    /**
     * Finds a recipe by its exact title.
     *
     * @param title the recipe title to search
     * @return an Optional containing the recipe if found
     */
    Optional<Recipe> findByTitle(String title);

    /**
     * Retrieves all recipes created by a specific user.
     *
     * @param user the user whose recipes to retrieve
     * @return a list of recipes created by the user
     */
    List<Recipe> findByUser(User user);

    /**
     * Finds a recipe by its ID.
     *
     * @param id the recipe ID
     * @return an Optional containing the recipe if found
     */
    Optional<Recipe> findById(Integer id);

    /**
     * Checks if a recipe exists for a given username and title.
     * Used to prevent duplicate recipe submissions by the same user.
     *
     * @param username the owner's username
     * @param title the recipe title
     * @return true if a matching recipe exists; false otherwise
     */
    boolean existsByUser_UsernameAndTitleIgnoreCase(String username, String title);
}
