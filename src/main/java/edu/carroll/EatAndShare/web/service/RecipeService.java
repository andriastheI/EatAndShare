package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RecipeService {

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

    List<Recipe> latestRecipes();

    Recipe getRecipeOrThrow(Integer id);

    // ✅ New method for category pages
    List<Recipe> findByCategoryName(String categoryName);

    // New pageable APIs
    Page<Recipe> latestRecipes(Pageable pageable);

    Page<Recipe> searchRecipes(String q, Pageable pageable);

}



