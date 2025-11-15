package edu.carroll.eatAndShare.backEnd.service;

import edu.carroll.eatAndShare.backEnd.model.Recipe;
import edu.carroll.eatAndShare.backEnd.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Filename: RecipeService.java
 * Author: Andrias and Selin
 * Date: October 11, 2025
 *
 * Description:
 * Service boundary for managing Recipe entities. Provides operations for
 * creating, retrieving, searching, and deleting recipes while enforcing
 * input validation, security, and consistent return values.
 */

public interface RecipeService {

    /**
     * Creates and saves a new recipe associated with the given user.
     *
     * @param title            the recipe title (must be non-null/non-blank)
     * @param prepTime         preparation time in minutes (must be non-null and non-negative)
     * @param cookTime         cooking time in minutes (must be non-null and non-negative)
     * @param difficulty       difficulty level (e.g., "Easy", "Medium", "Hard")
     * @param instructions     step-by-step cooking instructions
     * @param ingredientNames  list of ingredient names
     * @param quantities       list of ingredient quantities
     * @param units            list of ingredient units
     * @param categoryName     name of the category this recipe belongs to
     * @param image            uploaded image file (optional)
     * @param username         username of the recipe creator
     * @throws IllegalArgumentException if validation fails
     */
    void saveRecipe(String title,
                    Integer prepTime,
                    Integer cookTime,
                    String difficulty,
                    String instructions,
                    List<String> ingredientNames,
                    List<String> quantities,
                    List<String> units,
                    String categoryName,
                    MultipartFile image,
                    String username);

    /**
     * Returns all recipes ordered from newest to oldest.
     *
     * @return a non-null list of recipes
     */
    List<Recipe> latestRecipes();

    /**
     * Retrieves a single recipe by ID.
     *
     * @param id the recipe ID
     * @return the recipe if found
     * @throws IllegalArgumentException if id is null or not found
     */
    Recipe getRecipe(Integer id);

    /**
     * Finds recipes by category name.
     *
     * @param categoryName the category to search
     * @return a non-null list of recipes, possibly empty
     */
    List<Recipe> findByCategoryName(String categoryName);

    /**
     * Returns a paginated list of the newest recipes.
     *
     * @param pageable the pagination information
     * @return a non-null page of recipes
     * @throws IllegalArgumentException if pageable is invalid
     */
    Page<Recipe> latestRecipes(Pageable pageable);

    /**
     * Performs a free-text search for recipes.
     *
     * @param q        search text (may be null/blank)
     * @param pageable pagination information
     * @return a non-null page of recipes
     */
    Page<Recipe> searchRecipes(String q, Pageable pageable);

    /**
     * Finds recipes belonging to a given user.
     *
     * @param user the owner
     * @return a non-null list of recipes
     */
    List<Recipe> findByUser(User user);

    /**
     * Deletes a recipe if it belongs to a given user.
     *
     * @param id   the recipe ID
     * @param user the owner requesting deletion
     */
    void deleteRecipeByIdAndUser(Integer id, User user);
}
