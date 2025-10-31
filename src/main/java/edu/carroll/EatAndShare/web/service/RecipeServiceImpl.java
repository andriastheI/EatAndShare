package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.*;
import edu.carroll.EatAndShare.backEnd.repo.*;
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
 * Implementation of {@link RecipeService}.
 * Handles creation, storage, and retrieval of recipes, categories, and ingredients.
 */
@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeIngredientRepository recipeIngredientRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    private static final Logger log = LoggerFactory.getLogger(RecipeServiceImpl.class);

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
        log.info("✅ RecipeServiceImpl initialized successfully");
    }

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

        try {
            // ✅ 1. Find user
            User user = userRepo.findByUsername(username);
            if (user == null) {
                log.warn("User not found: {}", username);
                throw new IllegalArgumentException("User not found: " + username);
            }

            // ✅ 2. Fetch existing category (DO NOT create new ones)
            // Normalize category name capitalization (e.g., "breakfast" → "Breakfast")
            String normalizedCategory = categoryName.substring(0, 1).toUpperCase() +
                    categoryName.substring(1).toLowerCase();

            Category category = categoryRepo.findByCategoryNameIgnoreCase(normalizedCategory)
                    .orElseGet(() -> {
                        log.warn("⚠️ Category '{}' not found — creating it now", normalizedCategory);
                        Category newCategory = new Category();
                        newCategory.setCategoryName(
                                normalizedCategory.substring(0, 1).toUpperCase() + normalizedCategory.substring(1).toLowerCase()
                        );
                        return categoryRepo.save(newCategory);
                    });

            // ✅ 3. Ensure upload directory exists
            String baseDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(baseDir, uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory at {}", uploadPath);
            }

            // ✅ 4. Save image if provided
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                String safeName = Paths.get(image.getOriginalFilename()).getFileName().toString();
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path filePath = uploadPath.resolve(fileName);

                image.transferTo(filePath.toFile());
                imageUrl = "/uploads/" + fileName;
                log.info("Saved image for recipe '{}' to {}", title, imageUrl);
            }

            // ✅ 5. Save recipe
            Recipe recipe = new Recipe();
            recipe.setTitle(title);
            recipe.setPrepTimeMins(prepTime);
            recipe.setCookTimeMins(cookTime);
            recipe.setDifficulty(difficulty);
            recipe.setInstructions(instructions);
            recipe.setCategory(category);  // Properly linked category
            recipe.setUser(user);
            recipe.setImgURL(imageUrl);
            recipeRepo.save(recipe);

            log.info("Recipe '{}' saved successfully (id={}) under category '{}'",
                    title, recipe.getId(), category.getCategoryName());

            // ✅ 6. Link ingredients
            if (ingredientNames != null && !ingredientNames.isEmpty()) {
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
                    link.setQuantity(i < quantities.size() ? quantities.get(i) : "");
                    link.setUnit(i < units.size() ? units.get(i) : "");

                    recipeIngredientRepo.save(link);
                }
                log.info("Linked {} ingredients to recipe '{}'", ingredientNames.size(), title);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error saving recipe image: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error saving recipe: " + e.getMessage(), e);
        }
    }


    @Override
    public List<Recipe> latestRecipes() {
        List<Recipe> recipes = recipeRepo.findAllByOrderByIdDesc();
        log.debug("Loaded {} recipes for home feed", recipes.size());
        return recipes;
    }
    @Override
    public Page<Recipe> latestRecipes(Pageable pageable) {
        return recipeRepo.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public Recipe getRecipeOrThrow(Integer id) {
        return recipeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));
    }

    // ✅ New category filter method
    @Override
    public List<Recipe> findByCategoryName(String categoryName) {
        return recipeRepo.findByCategory_CategoryNameIgnoreCase(categoryName);
    }

    @Override
    public Page<Recipe> searchRecipes(String q, Pageable pageable) {
        if (q == null || q.trim().isEmpty()) {
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
