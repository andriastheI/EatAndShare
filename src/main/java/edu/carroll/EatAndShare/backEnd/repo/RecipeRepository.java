package edu.carroll.EatAndShare.backEnd.repo;

import edu.carroll.EatAndShare.backEnd.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for performing CRUD operations on {@link Recipe} entities.
 *
 * <p>This interface extends {@link JpaRepository}, providing all the standard
 * data-access operations such as saving, updating, deleting, and finding recipes.
 * It automatically connects to the <strong>recipe</strong> table through the
 * {@link Recipe} entity mapping.</p>
 *
 * <p>Custom query methods can also be defined here using Spring Data JPA’s
 * derived query syntax. Spring will automatically generate the SQL based
 * on the method name.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     List&lt;Recipe&gt; latestRecipes = recipeRepository.findAllByOrderByIdDesc();
 * </pre>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

    /**
     * Retrieves all recipes ordered by their ID in descending order.
     * <p>This is typically used to display the newest recipes first.</p>
     *
     * @return a list of all recipes sorted by ID (newest first)
     */
    List<Recipe> findAllByOrderByIdDesc();
}
