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

@Service
public class RecipeService {

    @Autowired private RecipeRepository recipeRepo;
    @Autowired private IngredientRepository ingredientRepo;
    @Autowired private RecipeIngredientRepository recipeIngredientRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CategoryRepository categoryRepo;

    @Value("${file.upload-dir}")
    private String uploadDir;  // injected from properties file

    /**
     * Saves a recipe along with its category, ingredients, and user.
     * Downloads the uploaded image into the local uploads directory and
     * stores its URL path in the database for future access.
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

            // ✅ Get real project folder (finalproject)
            String baseDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(baseDir, uploadDir);

            // ✅ Ensure the uploads directory exists
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String imageUrl = null;

            // ✅ Save the uploaded image
            if (image != null && !image.isEmpty()) {
                String safeName = Paths.get(image.getOriginalFilename()).getFileName().toString();
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path filePath = uploadPath.resolve(fileName);

                image.transferTo(filePath.toFile());

                imageUrl = "/uploads/" + fileName;
            }

            // ✅ 4. Create and save the recipe
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

            // ✅ 5. Link ingredients
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

    public List<Recipe> latestRecipes() {
        return recipeRepo.findAllByOrderByIdDesc();
    }

    public Recipe getRecipeOrThrow(Integer id) {
        return recipeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));
    }
}
