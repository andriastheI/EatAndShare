package edu.carroll.eatAndShare.backEnd.service;

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
import java.util.List;

/**
 * Filename: RecipeServiceImpl.java
 * Author: Andrias and Selin
 * Date: October 11, 2025
 *
 * Description:
 * Implementation of RecipeService responsible for handling recipe creation,
 * validation, image upload, ingredient mapping, and search operations.
 * This class orchestrates interactions between repositories while ensuring
 * clean validation, logging, error handling, and ownership enforcement.
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

    /** Upload directory used for storing recipe images. */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Constructor-based dependency injection for all repositories.
     * Ensures all dependencies are ready before service logic is executed.
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
     * Creates and saves a fully validated recipe, including category handling,
     * ingredient creation/linking, and optional image upload.
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
            // ----------------------------------------------
            // 1. USER RESOLUTION
            // ----------------------------------------------
            User user = userRepo.findByUsername(username);
            if (user == null) {
                log.warn("❌ User '{}' not found", username);
                throw new IllegalArgumentException("User not found: " + username);
            }

            // ----------------------------------------------
            // 2. BASIC FIELD VALIDATION
            // ----------------------------------------------
            String trimmed = title == null ? "" : title.trim().replaceAll("\\s+", " ");
            if (trimmed.isEmpty()) throw new IllegalArgumentException("Recipe title cannot be empty.");
            if (recipeRepo.existsByUser_UsernameAndTitleIgnoreCase(username, trimmed))
                throw new IllegalArgumentException("You already have a recipe named \"" + trimmed + "\".");

            // Category validation
            if (categoryName == null || categoryName.isBlank())
                throw new IllegalArgumentException("Recipe category name is invalid.");

            List<String> allowedCategories = List.of("Dinner", "Lunch", "Breakfast", "Dessert", "Salad");
            if (!allowedCategories.contains(categoryName))
                throw new IllegalArgumentException("Category not found");

            // Difficulty validation
            if (difficulty == null) throw new IllegalArgumentException("Recipe difficulty is null");
            List<String> validDifficulties = List.of("Easy", "Medium", "Hard");
            if (!validDifficulties.contains(difficulty))
                throw new IllegalArgumentException("Invalid difficulty");

            // Title validation
            if (title.length() >= 100) throw new IllegalArgumentException("Recipe title is too long");

            // Time validations
            if (prepTime == null || cookTime == null)
                throw new IllegalArgumentException("Prep or cook time cannot be null");
            if (prepTime <= 0 || cookTime <= 0)
                throw new IllegalArgumentException("Invalid prep or cook time");

            // Instructions validation
            if (instructions == null || instructions.isBlank())
                throw new IllegalArgumentException("Instructions cannot be empty");
            if (instructions.length() > 500000)
                throw new IllegalArgumentException("Instructions too long");

            // Ingredient validation
            if (ingredientNames == null || quantities == null || units == null)
                throw new IllegalArgumentException("Ingredients required");
            if (!(ingredientNames.size() == quantities.size() && ingredientNames.size() == units.size()))
                throw new IllegalArgumentException("Ingredient list sizes mismatch");

            // ----------------------------------------------
            // 3. CATEGORY NORMALIZATION + CREATION
            // ----------------------------------------------
            String normalizedCategory = categoryName.substring(0, 1).toUpperCase()
                    + categoryName.substring(1).toLowerCase();

            Category category = categoryRepo.findByCategoryNameIgnoreCase(normalizedCategory)
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setCategoryName(normalizedCategory);
                        return categoryRepo.save(newCat);
                    });

            // ----------------------------------------------
            // 4. ENSURE IMAGE DIRECTORY EXISTS
            // ----------------------------------------------
            String baseDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(baseDir, uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("📁 Created upload directory: {}", uploadPath);
            }

            // ----------------------------------------------
            // 5. IMAGE UPLOAD HANDLING
            // ----------------------------------------------
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                String safeName = Paths.get(image.getOriginalFilename()).getFileName().toString();
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path filePath = uploadPath.resolve(fileName);

                image.transferTo(filePath.toFile());
                imageUrl = "/uploads/" + fileName;

                log.debug("📷 Image uploaded: {}", imageUrl);
            }

            // ----------------------------------------------
            // 6. CREATE AND SAVE MAIN RECIPE
            // ----------------------------------------------
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

            // ----------------------------------------------
            // 7. INGREDIENT PROCESSING + LINK CREATION
            // ----------------------------------------------
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

            log.info("✅ saveRecipe SUCCESS — '{}'", title);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            log.error("Image save FAILED: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns all recipes sorted by newest first.
     */
    @Override
    public List<Recipe> latestRecipes() {
        List<Recipe> recipes = recipeRepo.findAllByOrderByIdDesc();
        if (recipes == null) {
            return List.of();
        }
        return recipes;


    }



    /**
     * Returns a paginated list of newest recipes.
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

        return recipeRepo.findAllByOrderByIdDesc(pageable);
    }

    /**
     * Retrieves a recipe by its ID or throws an exception.
     */
    @Override
    public Recipe getRecipe(Integer id) {
        if (id == null)
            throw new IllegalArgumentException("Recipe ID cannot be null");

        return recipeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));
    }

    /**
     * Returns all recipes matching a category name (case-insensitive).
     */
    @Override
    public List<Recipe> findByCategoryName(String categoryName) {
        if (categoryName == null) {
            return List.of();
        }

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
     * Performs a free-text search or returns newest recipes if query empty.
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

        if (q == null || q.trim().isEmpty()) {
            return latestRecipes(pageable);
        }

        return recipeRepo.search(q.trim(), pageable);
    }

    /**
     * Returns all recipes created by a specific user.
     */
    @Override
    public List<Recipe> findByUser(User user) {
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
     * Deletes a recipe only if it belongs to the given user.
     */
    @Override
    public boolean deleteRecipeByIdAndUser(Integer id, User user) {
        // Invalid inputs → nothing deleted
        if (id == null || user == null || user.getId() == null) {
            return false;
        }

        return recipeRepo.findById(id)
                .filter(recipe -> recipe.getUser() != null &&
                        user.getId().equals(recipe.getUser().getId()))
                .map(recipe -> {
                    recipeRepo.delete(recipe);
                    return true;
                })
                .orElse(false);
    }

}
