package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.*;
import edu.carroll.EatAndShare.backEnd.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service class responsible for managing recipes, including creation,
 * file uploads, and linking ingredients, categories, and users.
 *
 * <p>This class acts as the business logic layer between controllers
 * and repositories. It handles all interactions related to recipes,
 * ensuring that associated entities (user, category, ingredients)
 * are created or linked correctly, and uploaded images are stored
 * in a configured directory.</p>
 *
 * <p>Images are saved in a local folder defined by the
 * <strong>file.upload-dir</strong> property in
 * <code>application.properties</code>, and their web paths are stored
 * in the database as <code>/uploads/&lt;filename&gt;</code>.</p>
 *
 * <p>Relationships managed:</p>
 * <ul>
 *   <li>{@link User} – identifies which user created the recipe</li>
 *   <li>{@link Category} – categorizes the recipe (e.g., Dessert, Vegan)</li>
 *   <li>{@link Ingredient} – links all ingredient entries through {@link RecipeIngredient}</li>
 * </ul>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Service
public class RecipeService {

    /** Repository for performing CRUD operations on recipes. */
    @Autowired private RecipeRepository recipeRepo;

    /** Repository for ingredient lookups and creation. */
    @Autowired private IngredientRepository ingredientRepo;

    /** Repository for managing recipe-ingredient relationships. */
    @Autowired private RecipeIngredientRepository recipeIngredientRepo;

    /** Repository for retrieving user information. */
    @Autowired private UserRepository userRepo;

    /** Repository for retrieving or creating recipe categories. */
    @Autowired private CategoryRepository categoryRepo;

    /** Upload directory path injected from application properties. */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Saves a recipe along with its associated category, ingredients, and user.
     *
     * <p>This method performs multiple coordinated operations:</p>
     * <ol>
     *   <li>Retrieves the user who created the recipe.</li>
     *   <li>Finds or creates a recipe category.</li>
     *   <li>Ensures that the upload directory exists.</li>
     *   <li>Saves the uploaded image file and stores its path in the database.</li>
     *   <li>Creates the recipe and links all its ingredients.</li>
     * </ol>
     *
     * <p>If the upload fails or a repository operation encounters an error,
     * a {@link RuntimeException} is thrown with a descriptive message.</p>
     *
     * @param title         the recipe title
     * @param prepTime      preparation time in minutes
     * @param cookTime      cooking time in minutes
     * @param difficulty    difficulty level (Easy, Medium, Hard)
     * @param instructions  text instructions for preparing the recipe
     * @param ingredientNames list of ingredient names
     * @param quantities    corresponding ingredient quantities
     * @param units         corresponding measurement units
     * @param categoryName  category name (creates new if not found)
     * @param image         uploaded image file
     * @param username      username of the user creating the recipe
     * @throws RuntimeException if image saving or database operations fail
     */
    public void saveRecipe(String title,
                           Integer prepTime,
                           Integer cookTime,
                           String difficulty,
                           String instructions,
                           List<String> ingredientNames,
                           List<String> quantities,
                           List<String> units,
                           String categoryName,
                           MultipartFile image,
                           String username) {

        try {
            // ✅ 1. Get the user who created the recipe
            User user = userRepo.findByUsername(username);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + username);
            }

            // ✅ 2. Find or create category
            Category category = categoryRepo.findByCategoryName(categoryName)
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setCategoryName(categoryName);
                        return categoryRepo.save(newCat);
                    });

            // ✅ 3. Ensure the upload directory exists
            String baseDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(baseDir, uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String imageUrl = null;

            // ✅ 4. Save the uploaded image
            if (image != null && !image.isEmpty()) {
                String safeName = Paths.get(image.getOriginalFilename()).getFileName().toString();
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path filePath = uploadPath.resolve(fileName);

                image.transferTo(filePath.toFile());
                imageUrl = "/uploads/" + fileName; // Web-accessible path
            }

            // ✅ 5. Create and save the recipe entity
            Recipe recipe = new Recipe();
            recipe.setTitle(title);
            recipe.setPrepTimeMins(prepTime);
            recipe.setCookTimeMins(cookTime);
            recipe.setDifficulty(difficulty);
            recipe.setInstructions(instructions);
            recipe.setCategory(category);
            recipe.setUser(user);
            recipe.setImgURL(imageUrl);

            recipeRepo.save(recipe);

            // ✅ 6. Link ingredients through the join table
            if (ingredientNames != null && !ingredientNames.isEmpty()) {
                for (int i = 0; i < ingredientNames.size(); i++) {
                    String ingName = ingredientNames.get(i);
                    if (ingName == null || ingName.trim().isEmpty()) continue; // skip blanks

                    Ingredient ingredient = ingredientRepo.findByIngredientNameIgnoreCase(ingName.trim())
                            .orElseGet(() -> {
                                Ingredient newIng = new Ingredient();
                                newIng.setIngredientName(ingName.trim());
                                return ingredientRepo.save(newIng);
                            });

                    RecipeIngredient link = new RecipeIngredient();
                    link.setRecipe(recipe);
                    link.setIngredient(ingredient);
                    link.setQuantity(i < quantities.size() ? quantities.get(i) : "");
                    link.setUnit(i < units.size() ? units.get(i) : "");

                    recipeIngredientRepo.save(link);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error saving recipe image: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error saving recipe: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a list of all recipes ordered by their ID in descending order.
     * Used to display the newest recipes first on the home page.
     *
     * @return a list of the most recent recipes
     */
    public List<Recipe> latestRecipes() {
        return recipeRepo.findAllByOrderByIdDesc();
    }

    /**
     * Retrieves a recipe by ID or throws an exception if not found.
     * <p>This helper method simplifies controller logic by encapsulating
     * error handling in one place.</p>
     *
     * @param id the recipe ID
     * @return the recipe if found
     * @throws IllegalArgumentException if the recipe is not found
     */
    public Recipe getRecipeOrThrow(Integer id) {
        return recipeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));
    }
}
