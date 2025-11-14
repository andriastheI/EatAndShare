package edu.carroll.eatAndShare.web.service;

import edu.carroll.eatAndShare.backEnd.model.Recipe;
import edu.carroll.eatAndShare.backEnd.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Service boundary for managing {@link Recipe} entities.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Create and persist new recipes (including ingredients and image metadata).</li>
 *     <li>Fetch the latest recipes with or without pagination.</li>
 *     <li>Look up recipes by ID, category, free-text search, and owner.</li>
 *     <li>Delete recipes in a user-safe, ownership-aware way.</li>
 * </ul>
 * <p>
 * Implementations are expected to:
 * <ul>
 *     <li>Validate inputs and throw {@link IllegalArgumentException} for invalid data.</li>
 *     <li>Return empty collections or pages instead of {@code null} when no data is found.</li>
 *     <li>Enforce security/ownership checks when deleting by user.</li>
 * </ul>
 */

public interface RecipeService {

    /**
     * Creates and saves a new recipe associated with the given user.
     * <p>
     * Typical responsibilities of the implementation:
     * <ul>
     *     <li>Validate all required fields (title, times, difficulty, instructions, etc.).</li>
     *     <li>Validate list arguments (ingredientNames, quantities, units) for size and content.</li>
     *     <li>Normalize and/or validate the {@code categoryName}.</li>
     *     <li>Handle image metadata and storage (e.g., saving or copying the uploaded file).</li>
     *     <li>Associate the recipe with the user identified by {@code username}.</li>
     * </ul>
     *
     * @param title            the recipe title (must be non-null/non-blank)
     * @param prepTime         preparation time in minutes (must be non-null and non-negative)
     * @param cookTime         cooking time in minutes (must be non-null and non-negative)
     * @param difficulty       difficulty level (e.g., "Easy", "Medium", "Hard")
     * @param instructions     step-by-step cooking instructions
     * @param ingredientNames  list of ingredient names; must align in size with {@code quantities} and {@code units}
     * @param quantities       list of ingredient quantities, parallel to {@code ingredientNames}
     * @param units            list of ingredient units (e.g., "cups", "tsp"), parallel to {@code ingredientNames}
     * @param categoryName     name of the category this recipe belongs to (e.g., "Dessert")
     * @param image            uploaded image file for the recipe (may be optional depending on implementation)
     * @param username         username of the user who owns/created this recipe
     * @throws IllegalArgumentException if validation fails (e.g., null/blank fields, mismatched list sizes, unknown user)
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
     * <p>
     * Implementations should typically sort by descending ID or creation timestamp.
     *
     * @return a non-null list of recipes, possibly empty if no recipes exist
     */
    List<Recipe> latestRecipes();

    /**
     * Retrieves a single recipe by its unique ID.
     *
     * @param id the ID of the recipe to fetch; must not be {@code null}
     * @return the corresponding {@link Recipe} if found
     * @throws IllegalArgumentException if {@code id} is {@code null} or no recipe with that ID exists
     */
    Recipe getRecipe(Integer id);

    /**
     * Finds all recipes that belong to the given category name.
     * <p>
     * Implementations are expected to:
     * <ul>
     *     <li>Treat the category lookup as case-insensitive.</li>
     *     <li>Trim leading/trailing whitespace.</li>
     *     <li>Return an empty list if {@code categoryName} is {@code null} or no matches are found.</li>
     * </ul>
     *
     * @param categoryName the name of the category to search by (e.g., "Dessert");
     *                     may be {@code null}, in which case an empty list should be returned
     * @return a non-null list of recipes in that category, possibly empty
     */

    List<Recipe> findByCategoryName(String categoryName);

    /**
     * Returns a page of the latest recipes using the given pagination information.
     * <p>
     * Implementations should:
     * <ul>
     *     <li>Sort recipes from newest to oldest.</li>
     *     <li>Respect page index and size from {@link Pageable}.</li>
     *     <li>Throw an {@link IllegalArgumentException} if the {@code pageable} is invalid
     *         (e.g., negative page index).</li>
     * </ul>
     *
     * @param pageable paging information (page number, size, sort); must not be {@code null}
     * @return a non-null {@link Page} of recipes; may be empty if the requested page is beyond available data
     * @throws IllegalArgumentException if {@code pageable} is {@code null} or invalid
     */
    Page<Recipe> latestRecipes(Pageable pageable);

    /**
     * Performs a free-text search for recipes based on the query string.
     * <p>
     * Implementations should typically search on title and possibly other fields
     * (e.g., description, ingredients) in a case-insensitive way.
     * <p>
     * Special handling for {@code q}:
     * <ul>
     *     <li>If {@code q} is {@code null} or blank, this should behave like {@link #latestRecipes(Pageable)}.</li>
     *     <li>Non-matching queries must return an empty page, not {@code null}.</li>
     *     <li>Queries containing emojis or SQL-like payloads must be handled safely and not cause errors.</li>
     * </ul>
     *
     * @param q        search query text; may be {@code null} or blank to mean "no filter"
     * @param pageable paging information; must not be {@code null}
     * @return a non-null {@link Page} of matching recipes; possibly empty
     * @throws IllegalArgumentException if {@code pageable} is {@code null} or invalid
     */
    Page<Recipe> searchRecipes(String q, Pageable pageable);

    /**
     * Finds all recipes that belong to the given user.
     * <p>
     * Implementations should:
     * <ul>
     *     <li>Return an empty list if the user is {@code null}.</li>
     *     <li>Return an empty list if the user has no recipes or has not been persisted yet (no ID).</li>
     * </ul>
     *
     * @param user the owner whose recipes should be returned; may be {@code null}
     * @return a non-null list of recipes for that user, possibly empty
     */
    List<Recipe> findByUser(User user);

    /**
     * Deletes the recipe with the given ID if it belongs to the provided user.
     * <p>
     * Implementations should:
     * <ul>
     *     <li>Do nothing if {@code id} is {@code null}.</li>
     *     <li>Do nothing if {@code user} is {@code null}.</li>
     *     <li>Only delete the recipe if it exists and is owned by {@code user}.</li>
     *     <li>Be idempotent: calling this method multiple times with the same arguments should not throw.</li>
     * </ul>
     *
     * @param id   the ID of the recipe to delete; may be {@code null}
     * @param user the user requesting the deletion (used to enforce ownership); may be {@code null}
     */
    void deleteRecipeByIdAndUser(Integer id, User user);


}



