package edu.carroll.eatAndShare.backEnd.repo;

import edu.carroll.eatAndShare.backEnd.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link Ingredient} entities.
 *
 * <p>This interface extends {@link JpaRepository}, giving it access to all
 * standard database operations such as saving, deleting, and retrieving ingredients.
 * It automatically connects to the <strong>ingredient</strong> table through the
 * JPA entity mapping defined in {@link Ingredient}.</p>
 *
 * <p>In addition to the built-in methods from {@code JpaRepository},
 * this repository includes a custom query method for finding ingredients
 * by name, ignoring case sensitivity.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     Optional&lt;Ingredient&gt; ingredient =
 *         ingredientRepository.findByIngredientNameIgnoreCase("Sugar");
 * </pre>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

    /**
     * Finds an ingredient by its name, ignoring case sensitivity.
     *
     * @param ingredientName the name of the ingredient to search for
     * @return an {@link Optional} containing the ingredient if found, or empty if not found
     */
    Optional<Ingredient> findByIngredientNameIgnoreCase(String ingredientName);
}
