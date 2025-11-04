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

    private static final Logger log = LoggerFactory.getLogger(RecipeServiceImpl.class);

    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeIngredientRepository recipeIngredientRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;



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
        log.info("✅ RecipeServiceImpl initialized");
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

        log.info("➡️ saveRecipe START — user='{}', title='{}', category='{}'", username, title, categoryName);

        try {
            // ✅ Find user
            User user = userRepo.findByUsername(username);
            if (user == null) {
                log.warn("❌ saveRecipe FAILED — user not found: '{}'", username);
                throw new IllegalArgumentException("User not found: " + username);
            }
            log.debug("User resolved: {}", user.getUsername());

            String trimmed = title == null ? "" : title.trim().replaceAll("\\s+", " ");
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty.");
            }
            if (recipeRepo.existsByUser_UsernameAndTitleIgnoreCase(username, trimmed)) {
                throw new IllegalArgumentException("You already have a recipe named \"" + trimmed + "\".");
            }


            // ✅ Normalize category name
            String normalizedCategory = categoryName.substring(0, 1).toUpperCase()
                    + categoryName.substring(1).toLowerCase();

            Category category = categoryRepo.findByCategoryNameIgnoreCase(normalizedCategory)
                    .orElseGet(() -> {
                        log.warn("⚠️ Category '{}' not found — creating", normalizedCategory);
                        Category newCategory = new Category();
                        newCategory.setCategoryName(normalizedCategory);
                        return categoryRepo.save(newCategory);
                    });

            // ✅ Ensure upload directory exists
            String baseDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(baseDir, uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("📁 Created upload directory at {}", uploadPath);
            }

            // ✅ Save image
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                String safeName = Paths.get(image.getOriginalFilename()).getFileName().toString();
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path filePath = uploadPath.resolve(fileName);

                image.transferTo(filePath.toFile());
                imageUrl = "/uploads/" + fileName;
                log.debug("📷 Image uploaded: {}", imageUrl);
            }

            // ✅ Create recipe
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
            log.info("✅ Recipe saved — id={}, title='{}', category='{}'", recipe.getId(), title, category.getCategoryName());

            // ✅ Handle ingredients
            if (ingredientNames != null && !ingredientNames.isEmpty()) {
                for (int i = 0; i < ingredientNames.size(); i++) {
                    String ingName = ingredientNames.get(i);
                    if (ingName == null || ingName.trim().isEmpty()) continue;

                    Ingredient ingredient = ingredientRepo.findByIngredientNameIgnoreCase(ingName.trim())
                            .orElseGet(() -> {
                                log.debug("➕ Ingredient '{}' not found — creating", ingName);
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
                log.info("🔗 Linked {} ingredients to recipe '{}'", ingredientNames.size(), title);
            }

            log.info("✅ saveRecipe SUCCESS — recipe '{}' saved", title);

        } catch (IOException e) {
            log.error("❌ Image save FAILED: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving recipe image: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("❌ saveRecipe FAILED: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving recipe: " + e.getMessage(), e);
        }
    }


    @Override
    public List<Recipe> latestRecipes() {
        List<Recipe> recipes = recipeRepo.findAllByOrderByIdDesc();
        log.debug("latestRecipes: fetched {} recipes", recipes.size());
        return recipes;
    }

    @Override
    public Page<Recipe> latestRecipes(Pageable pageable) {
        log.debug("latestRecipes paginated request");
        return recipeRepo.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public Recipe getRecipe(Integer id) {
        log.debug("Fetching recipe id={}", id);
        if (id == null) {
            log.warn("getRecipeOrThrow FAILED — id was null");
            throw new IllegalArgumentException("Recipe ID cannot be null");
        }
        return recipeRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ getRecipe FAILED — recipe not found id={}", id);
                    return new IllegalArgumentException("Recipe not found: " + id);
                });
    }

    // ✅ New category filter method
    @Override
    public List<Recipe> findByCategoryName(String categoryName) {
        log.debug("Searching recipes by category '{}'", categoryName);

        if (categoryName == null) {
            return List.of();
        }

        // Use strip() (Unicode-aware) then collapse internal runs of whitespace
        String normalized = categoryName.strip().replaceAll("\\s+", " ");
        if (normalized.isEmpty()){
            return List.of();
        }

        return recipeRepo.findByCategory_CategoryNameIgnoreCase(normalized);
    }

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
        if (user == null || user.getId() == null) {
            log.warn("user or user.id is null");
            return List.of();
        }
        return recipeRepo.findByUser(user);
    }


    @Override
    public void deleteRecipeByIdAndUser(Integer id, User user) {
        if (id == null || user == null) {
            log.warn("deleteRecipe: Skipped — null id or user");
            return;
            //throw new IllegalArgumentException("Recipe ID or User ID cannot be null");
        }
        recipeRepo.findById(id).ifPresent(recipe -> {
            if (recipe.getUser().getId().equals(user.getId())) {
                recipeRepo.delete(recipe);
            }
        });
    }

}
