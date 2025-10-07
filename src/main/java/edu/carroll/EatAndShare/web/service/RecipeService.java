package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.*;
import edu.carroll.EatAndShare.backEnd.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    @Autowired private RecipeRepository recipeRepo;
    @Autowired private IngredientRepository ingredientRepo;
    @Autowired private RecipeIngredientRepository recipeIngredientRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CategoryRepository categoryRepo;

    public void saveRecipe(String title, Integer prepTime, Integer cookTime, String difficulty,
                           String instructions, List<String> ingredientNames, List<String> quantities,
                           List<String> units, String categoryName, MultipartFile image, Principal principal) {

        // 1️Get the logged-in user
        User user = userRepo.findByUsername(principal.getName());

        // 2️Create and save Recipe
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setTitle(title);
        recipe.setPrepTimeMins(prepTime);
        recipe.setCookTimeMins(cookTime);
        recipe.setDifficulty(difficulty);
        recipe.setImgURL("/uploads/" + (image != null ? image.getOriginalFilename() : "default.jpg"));
        // ✅ Add this once your Recipe class has an instructions field
        recipe.setInstructions(instructions);


        // Get or create category
        Optional<Category> catOptional = categoryRepo.findByCategoryNameIgnoreCase(categoryName);
        Category category = catOptional.orElseGet(() -> {
            Category newCat = new Category();
            newCat.setCategoryName(categoryName);
            return categoryRepo.save(newCat);
        });
        recipe.setCategory(category);

        recipeRepo.save(recipe);
        // 3️Handle ingredients + linking
        for (int i = 0; i < ingredientNames.size(); i++) {
            String name = ingredientNames.get(i).trim();
            String qty = quantities.get(i).trim();
            String unit = units.get(i).trim();

            // Check if ingredient already exists
            Optional<Ingredient> existingIng = ingredientRepo.findByIngredientNameIgnoreCase(name);
            Ingredient ingredient = existingIng.orElseGet(() -> {
                Ingredient newIng = new Ingredient();
                newIng.setIngredientName(name);
                return ingredientRepo.save(newIng);
            });

            // Create and save RecipeIngredient link
            RecipeIngredient link = new RecipeIngredient();
            link.setRecipe(recipe);
            link.setIngredient(ingredient);
            link.setQuantity(qty);
            link.setUnit(unit);
            recipeIngredientRepo.save(link);
        }
    }
}
