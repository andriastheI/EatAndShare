package edu.carroll.eatAndShare.web.service;

import edu.carroll.eatAndShare.backEnd.model.*;
import edu.carroll.eatAndShare.backEnd.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link RecipeService}.
 * <p>
 * This service handles:
 * <ul>
 *   <li>Creating and saving recipes</li>
 *   <li>Uploading/storing images to a server directory</li>
 *   <li>Creating ingredients and linking them to the recipe</li>
 *   <li>Searching and retrieving recipes</li>
 *   <li>Ensuring users and categories exist before associations</li>
 * </ul>
 * </p>
 */
@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private static final Logger log = LoggerFactory.getLogger(RecipeServiceImpl.class);

    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeIngredientRepository recipeIngredientRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    /**
     * Constructor-based dependency injection for repositories.
     *
     * @param recipeRepo           Repository for recipes
     * @param ingredientRepo       Repository for ingredients
     * @param recipeIngredientRepo Repository for linking recipe + ingredient
     * @param userRepo             Repository for user lookup
     * @param categoryRepo         Repository for recipe categories
     */

    // why: provide safe default so NPE won't occur if property missing
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // ✅ Constructor-based dependency injection
    public RecipeServiceImpl(RecipeRepository recipeRepo,
                             IngredientRepository ingredientRepo,
                             RecipeIngredientRepository recipeIngredientRepo,
                             UserRepository userRepo,
                             CategoryRepository categoryRepo) {
        this.recipeRepo = recipeRepo;
        this.ingredientRepo = ingredientRepo;
        this.recipeIngredientRepo = recipeIngredientRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        log.info("RecipeServiceImpl initialized");
    }

    /**
     * Saves a new recipe along with optional image upload and ingredient creation.
     * <p>
     * Responsibilities:
     * <ul>
     *   <li>Validates user exists</li>
     *   <li>Creates category if needed</li>
     *   <li>Persists recipe entity</li>
     *   <li>Handles image upload to configured directory</li>
     *   <li>Creates ingredients & links them to recipe (RecipeIngredient many-to-many table)</li>
     * </ul>
     * </p>
     *
     * @param title           Recipe title
     * @param prepTime        Preparation time (minutes)
     * @param cookTime        Cooking time (minutes)
     * @param difficulty      Difficulty label (Easy, Medium, Hard)
     * @param instructions    Text description of cooking steps
     * @param ingredientNames List of ingredient names
     * @param quantities      Corresponding ingredient quantity string values
     * @param units           Unit of measurement corresponding to each ingredient
     * @param categoryName    Category (Breakfast, Lunch, Dinner, etc.)
     * @param image           Image uploaded by user (nullable)
     * @param username        Username of recipe creator
     *
     * @throws IllegalArgumentException If user does not exist
     * @throws RuntimeException         If recipe saving fails
     */
    /**
     * Saves a recipe with ingredients, category, difficulty, and optional image upload.
     *
     * @param title            Recipe title (required, <= 100 chars)
     * @param prepTime         Prep time in minutes (required, > 0)
     * @param cookTime         Cook time in minutes (required, > 0)
     * @param difficulty       Difficulty level: Easy / Medium / Hard (required)
     * @param instructions     Instructions text (required, not blank, <= 500k chars)
     * @param ingredientNames  List of ingredients (required, sizes must match quantities & units)
     * @param quantities       List of ingredient quantities
     * @param units            List of ingredient units for each ingredient
     * @param categoryName     Recipe category (Dinner, Lunch, Breakfast, Dessert, Salad)
     * @param image            Optional uploaded image
     * @param username         Username of the logged-in user saving the recipe
     *
     * @throws IllegalArgumentException if any validation fails
     */
    @Override
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

        log.info("saveRecipe START — user='{}', title='{}', category='{}'", username, title, categoryName);

        try {
            // ✅ Validate user exists
            User user = userRepo.findByUsername(username);
            if (user == null) {
                log.warn("saveRecipe FAILED — user not found: '{}'", username);
                throw new IllegalArgumentException("User not found");
            }

            // ✅ Validate category
            if (categoryName == null) {
                log.warn("Recipe category name is null");
                throw new IllegalArgumentException("Recipe category name is null");
            }

            if (categoryName.isBlank()) {
                log.warn("Recipe category name is blank");
                throw new IllegalArgumentException("Recipe category name is Invalid");
            }

            List<String> allowedCategories = List.of("Dinner", "Lunch", "Breakfast", "Dessert", "Salad");
            if (!allowedCategories.contains(categoryName)) {
                log.warn("Recipe category '{}' not found", categoryName);
                throw new IllegalArgumentException("Category not found");
            }

            // ✅ Validate difficulty
            if (difficulty == null) {
                log.warn("Recipe difficulty is null");
                throw new IllegalArgumentException("Recipe difficulty is null");
            }

            List<String> validDifficulties = List.of("Easy", "Medium", "Hard");
            if (!validDifficulties.contains(difficulty)) {
                log.warn("Invalid difficulty '{}'", difficulty);
                throw new IllegalArgumentException("Invalid difficulty level");
            }

            // ✅ Validate title
            if (title == null || title.isBlank()) {
                log.warn("Recipe title is blank or null");
                throw new IllegalArgumentException("Recipe title cannot be empty");
            }

            if (title.length() >= 100) {
                log.warn("Recipe title too long");
                throw new IllegalArgumentException("Recipe title is too long");
            }

            // ✅ Validate times
            if (prepTime == null || cookTime == null) {
                log.warn("Prep or cook time is null");
                throw new IllegalArgumentException("Recipe cook or prep time cannot be null");
            }

            if (prepTime <= 0) {
                log.warn("Invalid prepTime '{}'", prepTime);
                throw new IllegalArgumentException("Recipe prepTime is too low");
            }

            if (cookTime <= 0) {
                log.warn("Invalid cookTime '{}'", cookTime);
                throw new IllegalArgumentException("Recipe cookTime is too low");
            }

            // ✅ Validate instructions
            if (instructions == null) {
                log.warn("Recipe instructions not provided");
                throw new IllegalArgumentException("Recipe instructions is not provided");
            }

            if (instructions.isBlank()) {
                log.warn("Recipe instructions blank");
                throw new IllegalArgumentException("Recipe instructions is blank");
            }

            if (instructions.length() > 500000) {
                log.warn("Recipe instructions too long");
                throw new IllegalArgumentException("Recipe instructions is too long");
            }

            // ✅ Validate ingredients
            if (ingredientNames == null || quantities == null || units == null) {
                log.warn("Ingredient list or quantities/units are null");
                throw new IllegalArgumentException("Ingredients required");
            }

            if (!(ingredientNames.size() == quantities.size() && ingredientNames.size() == units.size())) {
                log.warn("Ingredient list sizes mismatch");
                throw new IllegalArgumentException("ingredient list sizes mismatch");
            }

            // Normalize category
            String normalizedCategory = categoryName.substring(0, 1).toUpperCase()
                    + categoryName.substring(1).toLowerCase();

            // ✅ Retrieve or create category
            Category category = categoryRepo.findByCategoryNameIgnoreCase(normalizedCategory)
                    .orElseGet(() -> {
                        log.warn("Category '{}' not found — creating new category", normalizedCategory);
                        Category newCategory = new Category();
                        newCategory.setCategoryName(normalizedCategory);
                        return categoryRepo.save(newCategory);
                    });

            // ✅ Ensure upload directory exists
            String baseDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(baseDir, uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory at {}", uploadPath);
            }

            // ✅ Save image if provided
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                String safeName = Paths.get(image.getOriginalFilename()).getFileName().toString();
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path filePath = uploadPath.resolve(fileName);

                image.transferTo(filePath.toFile());
                imageUrl = "/uploads/" + fileName;
                log.debug("📷 Image uploaded: {}", imageUrl);
            }

            // ✅ Create and save recipe record
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
            log.info("Recipe saved — id={}, title='{}', category='{}'",
                    recipe.getId(), title, category.getCategoryName());

            // ✅ Link ingredients
            for (int i = 0; i < ingredientNames.size(); i++) {
                String ingName = ingredientNames.get(i);
                if (ingName == null || ingName.trim().isEmpty()) continue;

                Ingredient ingredient = ingredientRepo.findByIngredientNameIgnoreCase(ingName.trim())
                        .orElseGet(() -> {
                            Ingredient newIng = new Ingredient();
                            newIng.setIngredientName(ingName.trim());
                            return ingredientRepo.save(newIng);
                        });

                RecipeIngredient link = new RecipeIngredient();
                link.setRecipe(recipe);
                link.setIngredient(ingredient);
                link.setQuantity(quantities.get(i));
                link.setUnit(units.get(i));

                recipeIngredientRepo.save(link);
            }

            log.info("saveRecipe SUCCESS — recipe '{}' saved", title);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            log.error("Image save FAILED: {}", e.getMessage(), e);
        }
    }

    /**
     * Retrieves the newest recipes (no paging).
     *
     * @return List of recipes ordered by newest first
     */
    @Override
    public List<Recipe> latestRecipes() {
        List<Recipe> recipes = recipeRepo.findAllByOrderByIdDesc();
        log.debug("latestRecipes: fetched {} recipes", recipes.size());
        return recipes;
    }

    /**
     * Retrieves newest recipes with pagination.
     *
     * @param pageable Pagination configuration (page number, size, sort order)
     * @return Page of recipes in newest-first order
     */
    @Override
    public Page<Recipe> latestRecipes(Pageable pageable) {
        log.debug("latestRecipes paginated request");
        return recipeRepo.findAllByOrderByIdDesc(pageable);
    }

    /**
     * Fetches recipe by ID or throws an exception if not found.
     *
     * @param id Recipe ID
     * @return Recipe entity
     * @throws IllegalArgumentException When recipe does not exist
     */
    @Override
    public Recipe getRecipeOrThrow(Integer id) {
        log.debug("Fetching recipe id={}", id);
        return recipeRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("getRecipeOrThrow FAILED — recipe not found id={}", id);
                    return new IllegalArgumentException("Recipe not found: " + id);
                });
    }
    /**
     * Retrieves recipes belonging to a specific category.
     *
     * @param categoryName Category name (case-insensitive)
     * @return List of recipes
     */
    @Override
    public List<Recipe> findByCategoryName(String categoryName) {
        log.debug("Searching recipes by category '{}'", categoryName);
        return recipeRepo.findByCategory_CategoryNameIgnoreCase(categoryName);
    }

    /**
     * Searches recipes based on query term. If query is blank, returns newest recipes instead.
     *
     * @param q        Search keyword (title or ingredient)
     * @param pageable Pagination settings
     * @return Page of recipes
     */
    @Override
    public Page<Recipe> searchRecipes(String q, Pageable pageable) {
        log.debug("searchRecipes query='{}'", q);
        if (q == null || q.trim().isEmpty()) {
            log.debug("Empty search — returning latest recipes");
            return latestRecipes(pageable);
        }
        return recipeRepo.search(q.trim(), pageable);
    }

    @Override
    public List<Recipe> findByUser(User user) {
        return recipeRepo.findByUser(user);
    }

    @Override
    public void deleteRecipeByIdAndUser(Integer id, User user) {
        recipeRepo.findById(id).ifPresent(recipe -> {
            if (recipe.getUser().getId().equals(user.getId())) {
                recipeRepo.delete(recipe);
            }
        });
    }

}
