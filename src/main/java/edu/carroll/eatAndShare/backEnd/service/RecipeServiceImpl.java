package edu.carroll.eatAndShare.backEnd.service;

import edu.carroll.eatAndShare.backEnd.model.*;
import edu.carroll.eatAndShare.backEnd.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Filename: RecipeServiceImpl.java
 * Description:
 * Implementation of RecipeService responsible for handling recipe creation,
 * validation, image upload, ingredient mapping, and search operations.
 * This class orchestrates interactions between repositories while ensuring
 * clean validation, logging, error handling, and ownership enforcement.
 */
@Service
public class RecipeServiceImpl implements RecipeService {

    /** Logger for debugging and tracing service behavior. */
    private static final Logger log = LoggerFactory.getLogger(RecipeServiceImpl.class);
    /** Repository for CRUD operations on recipes. */
    private final RecipeRepository recipeRepo;
    /** Repository for CRUD operations on ingredients. */
    private final IngredientRepository ingredientRepo;
    /** Repository for CRUD operations on recipe–ingredient link entities. */
    private final RecipeIngredientRepository recipeIngredientRepo;
    /** Repository for accessing and querying users. */
    private final UserRepository userRepo;
    /** Repository for accessing recipe categories. */
    private final CategoryRepository categoryRepo;

    /** Upload directory used for storing recipe images. */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Creates a new {@code RecipeServiceImpl} instance with all required dependencies.
     * <p>
     * Constructor-based dependency injection ensures that all repositories
     * are non-null and ready before any service method is invoked.
     *
     * @param recipeRepo             repository for recipes
     * @param ingredientRepo         repository for ingredients
     * @param recipeIngredientRepo   repository for recipe-ingredient links
     * @param userRepo               repository for users
     * @param categoryRepo           repository for categories
     */
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

        log.info("✅ RecipeServiceImpl initialized");
    }

    /**
     * Creates and persists a fully validated {@link Recipe} for the given user.
     * <p>
     * This method performs several steps:
     * <ol>
     *     <li>Resolve and validate the owning {@link User}.</li>
     *     <li>Validate basic recipe fields (title, category, difficulty, times, instructions).</li>
     *     <li>Validate ingredient lists (size and content).</li>
     *     <li>Resolve or create the {@link Category} entity.</li>
     *     <li>Handle optional image upload and store the file on disk.</li>
     *     <li>Create and save the main {@link Recipe} entity.</li>
     *     <li>Create or reuse {@link Ingredient} entities and link them via {@link RecipeIngredient}.</li>
     * </ol>
     * If any validation fails, an {@link IllegalArgumentException} is thrown
     * and no partial recipe is created.
     *
     * @param title           recipe title; must be non-null, non-empty, and shorter than 100 characters
     * @param prepTime        preparation time in minutes; must be positive
     * @param cookTime        cooking time in minutes; must be positive
     * @param difficulty      difficulty label; must be one of {@code "Easy"}, {@code "Medium"}, {@code "Hard"}
     * @param instructions    full recipe instructions; must be non-empty and below the max length
     * @param ingredientNames list of ingredient names; same size as {@code quantities} and {@code units}
     * @param quantities      list of quantities for each ingredient
     * @param units           list of units for each ingredient quantity
     * @param categoryName    readable category name (e.g., "Dinner"); must be in the allowed list
     * @param image           optional image file to be uploaded and linked to the recipe; may be {@code null}
     * @param username        username of the owner creating the recipe; must exist in the database
     *
     * @throws IllegalArgumentException if validation fails (user not found, invalid fields, etc.)
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

        log.info("➡️ saveRecipe START — user='{}', title='{}', category='{}'", username, title, categoryName);

        try {
            // ==========================================================
            // 1. USER RESOLUTION
            // Ensure that the username maps to a valid, persisted User.
            // ==========================================================
            User user = userRepo.findByUsername(username);
            if (user == null) {
                log.warn("❌ User '{}' not found", username);
                throw new IllegalArgumentException("User not found: " + username);
            }

            // ==========================================================
            // 2. BASIC FIELD VALIDATION (title, category, difficulty, time, instructions)
            //    Validate title, category, difficulty, times, and instructions
            //    BEFORE touching the database for recipes/ingredients.
            // ==========================================================

            // Normalize title by trimming and collapsing internal whitespace
            String trimmedTitle = title == null ? "" : title.trim().replaceAll("\\s+", " ");
            if (trimmedTitle.isEmpty())
                throw new IllegalArgumentException("Recipe title cannot be empty.");
            // Enforce uniqueness of title per user (case-insensitive)
            if (recipeRepo.existsByUser_UsernameAndTitleIgnoreCase(username, trimmedTitle))
                throw new IllegalArgumentException("You already have a recipe title saved");

            // ----- Category validation -----
            if (categoryName == null || categoryName.isBlank())
                throw new IllegalArgumentException("Recipe category name is invalid.");
            // Allowed categories are currently hard-coded, but could come from DB/config later
            List<String> allowedCategories = List.of("Dinner", "Lunch", "Breakfast", "Dessert", "Salad");
            if (!allowedCategories.contains(categoryName))
                throw new IllegalArgumentException("Category not found");

            // ----- Difficulty -----
            if (difficulty == null)
                throw new IllegalArgumentException("Recipe difficulty is null");
            List<String> validDifficulties = List.of("Easy", "Medium", "Hard");
            if (!validDifficulties.contains(difficulty))
                throw new IllegalArgumentException("Invalid difficulty");

            // ----- Title length -----
            if (title.length() >= 100)
                throw new IllegalArgumentException("Recipe title is too long");

            // ----- Time validations -----
            if (prepTime == null || cookTime == null)
                throw new IllegalArgumentException("Prep or cook time cannot be null");
            if (prepTime <= 0 || cookTime <= 0)
                throw new IllegalArgumentException("Invalid prep or cook time");

            // ----- Instructions -----
            if (instructions == null || instructions.isBlank())
                throw new IllegalArgumentException("Instructions cannot be empty");
            if (instructions.length() > 500000)
                throw new IllegalArgumentException("Instructions too long");

            // ==========================================================
            // 3. INGREDIENT LIST VALIDATION (SIZE + CONTENT)
            //    Ensure parallel lists (names, quantities, units) are consistent
            //    and contain non-empty ingredient names.
            // ==========================================================
            if (ingredientNames == null || quantities == null || units == null)
                throw new IllegalArgumentException("Ingredients required");

            // All ingredient-related lists must be the same length
            if (!(ingredientNames.size() == quantities.size() && ingredientNames.size() == units.size()))
                throw new IllegalArgumentException("Ingredient list sizes mismatch");

            // STRICT INGREDIENT VALIDATION BEFORE SAVING ANYTHING
            for (String ingName : ingredientNames) {
                if (ingName == null || ingName.trim().isEmpty())
                    throw new IllegalArgumentException("Ingredient name cannot be blank");
            }

            // ==========================================================
            // 4. CATEGORY LOOKUP OR CREATION
            //    Normalize the category label and either reuse an existing
            //    Category or create a new one.
            // ==========================================================
            String normalizedCategory =
                    categoryName.substring(0, 1).toUpperCase() +
                            categoryName.substring(1).toLowerCase();

            // Try to find category; if not present, create and save a new one
            Category category = categoryRepo.findByCategoryNameIgnoreCase(normalizedCategory)
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setCategoryName(normalizedCategory);
                        return categoryRepo.save(newCat);
                    });

            // ==========================================================
            // 5. IMAGE UPLOAD HANDLING
            //    If an image is provided, create the upload directory (if needed)
            //    and save the file to disk. Store its URL in imageUrl.
            // ==========================================================
            String imageUrl = null;
            String baseDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(baseDir, uploadDir);

            // Create directory if not exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("📁 Created upload directory: {}", uploadPath);
            }

            // Save image if provided
            if (image != null && !image.isEmpty()) {
                // Use only the last path component to avoid directory traversal issues
                String safeName = Paths.get(image.getOriginalFilename()).getFileName().toString();
                // Prepend timestamp to reduce filename collisions
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path filePath = uploadPath.resolve(fileName);
                // Persist image file to disk
                image.transferTo(filePath.toFile());
                imageUrl = "/uploads/" + fileName;

                log.debug("📷 Image uploaded: {}", imageUrl);
            }

            // ==========================================================
            // 6. CREATE AND SAVE MAIN RECIPE (AFTER ALL VALIDATION)
            //    At this point, all validation is complete. Create the Recipe
            //    and save it so we have a persistent ID for the link table.
            // ==========================================================
            Recipe recipe = new Recipe();
            recipe.setTitle(title);
            recipe.setPrepTimeMins(prepTime);
            recipe.setCookTimeMins(cookTime);
            recipe.setDifficulty(difficulty);
            recipe.setInstructions(instructions);
            recipe.setCategory(category);
            recipe.setUser(user);
            recipe.setImgURL(imageUrl);

            // SAVE RECIPE FIRST — ensures recipe ID exists
            recipeRepo.save(recipe);

            // ==========================================================
            // 7. INGREDIENT PROCESSING + LINK CREATION
            //    For each entry in the ingredient lists:
            //      - Find or create the Ingredient entity.
            //      - Create a RecipeIngredient link (join table row).
            // ==========================================================
            for (int i = 0; i < ingredientNames.size(); i++) {
                String ingName = ingredientNames.get(i).trim();

                // Find or create ingredient
                Ingredient ingredient = ingredientRepo.findByIngredientNameIgnoreCase(ingName)
                        .orElseGet(() -> {
                            Ingredient newIng = new Ingredient();
                            newIng.setIngredientName(ingName);
                            return ingredientRepo.save(newIng);
                        });

                // Create RecipeIngredient link
                RecipeIngredient link = new RecipeIngredient();
                link.setRecipe(recipe);
                link.setIngredient(ingredient);
                link.setQuantity(quantities.get(i));
                link.setUnit(units.get(i));

                recipeIngredientRepo.save(link);
            }

            log.info("✅ saveRecipe SUCCESS — '{}'", title);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            // I/O exception only affects image saving; recipe may or may not have been persisted,
            // but we log here so that the issue is visible in server logs.
            log.error("Image save FAILED: {}", e.getMessage(), e);
        }
    }

    /**
     * Retrieves all recipes in the system ordered by newest first.
     * <p>
     * This is a non-paginated variant intended for internal use or small datasets.
     *
     * @return list of recipes ordered by descending id; never {@code null}
     */
    @Override
    public List<Recipe> latestRecipes() {
        // Ask repository for all recipes, newest first
        List<Recipe> recipes = recipeRepo.findAllByOrderByIdDesc();
        // Normalize null return to an empty, immutable list
        if (recipes == null) {
            return List.of();
        }
        return recipes;


    }


    /**
     * Retrieves a page of recipes ordered by newest first using Spring Data pagination.
     *
     * @param pageable Spring Data {@link Pageable} describing page index and size;
     *                 must be non-null, with non-negative page index and positive page size
     * @return page of recipes ordered by descending id
     *
     * @throws IllegalArgumentException if {@code pageable} is invalid
     */
    @Override
    public Page<Recipe> latestRecipes(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }

        // Delegate to repository to handle pagination and sorting
        return recipeRepo.findAllByOrderByIdDesc(pageable);
    }

    /**
     * Retrieves a single recipe by its primary key.
     *
     * @param id unique identifier of the recipe to load; must not be {@code null}
     * @return the matching {@link Recipe}
     *
     * @throws IllegalArgumentException if {@code id} is null or recipe is not found
     */
    @Override
    public Recipe getRecipe(Integer id) {
        if (id == null)
            throw new IllegalArgumentException("Recipe ID cannot be null");

        // Use Optional to provide a clear exception message if not present
        return recipeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));
    }

    /**
     * Finds all recipes belonging to the given category name, case-insensitively.
     *
     * @param categoryName category name to search by; may be null or blank
     * @return list of recipes for the category; empty list if categoryName is invalid
     *         or no recipes exist. Never {@code null}.
     */
    @Override
    public List<Recipe> findByCategoryName(String categoryName) {
        if (categoryName == null) {
            return List.of();
        }

        // Normalize spaces to avoid issues with accidental extra whitespace
        String normalized = categoryName.strip().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return List.of();
        }

        List<Recipe> recipes = recipeRepo.findByCategory_CategoryNameIgnoreCase(normalized);
        if (recipes == null) {
            return List.of();
        }
        return recipes;
    }

    /**
     * Performs a free-text search over recipes, or falls back to {@link #latestRecipes(Pageable)}
     * when the search query is null or empty.
     *
     * @param q         search query (e.g., part of title or ingredient); may be null/blank
     * @param pageable  pagination information; must be non-null and valid
     * @return a page of recipes matching the search or the newest recipes if {@code q} is empty
     *
     * @throws IllegalArgumentException if {@code pageable} is invalid
     */
    @Override
    public Page<Recipe> searchRecipes(String q, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }

        // If no search term, just return latest recipes for the given page
        if (q == null || q.trim().isEmpty()) {
            return latestRecipes(pageable);
        }

        // Delegate text matching logic to repository (e.g., @Query or full-text search)
        return recipeRepo.search(q.trim(), pageable);
    }

    /**
     * Returns all recipes created by a particular user.
     *
     * @param user the user whose recipes should be fetched; if null or has null id,
     *             an empty list is returned
     * @return list of recipes owned by the user; never {@code null}
     */
    @Override
    public List<Recipe> findByUser(User user) {
        // If user is not persisted or not provided, no recipes can be found
        if (user == null || user.getId() == null) {
            return List.of();
        }

        List<Recipe> recipes = recipeRepo.findByUser(user);
        if (recipes == null) {
            return List.of();
        }
        return recipes;
    }

    /**
     * Deletes a recipe only if the given user is the owner.
     * <p>
     * This method enforces ownership so that one user cannot delete another
     * user's recipe. If the recipe is found and ownership matches, it is
     * deleted and the method returns {@code true}. Otherwise, the method
     * returns {@code false} and no changes are made.
     *
     * @param id   id of the recipe to delete; may be null
     * @param user user requesting deletion; must be non-null and persisted
     * @return {@code true} if a recipe was found and deleted; {@code false} otherwise
     */
    @Override
    public boolean deleteRecipeByIdAndUser(Integer id, User user) {
        // Invalid inputs → nothing deleted
        if (id == null || user == null || user.getId() == null) {
            return false;
        }

        return recipeRepo.findById(id)
                // Only allow deletion if recipe has an owner and it matches this user
                .filter(recipe -> recipe.getUser() != null &&
                        user.getId().equals(recipe.getUser().getId()))
                .map(recipe -> {
                    recipeRepo.delete(recipe);
                    return true;
                })
                // If not found or owner mismatch, return false
                .orElse(false);
    }

}
