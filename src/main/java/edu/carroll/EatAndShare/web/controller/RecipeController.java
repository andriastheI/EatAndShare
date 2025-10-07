package edu.carroll.EatAndShare.web.controller;


import edu.carroll.EatAndShare.web.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @PostMapping("/uploadRecipe")
    public String uploadRecipe(
            @RequestParam("title") String title,
            @RequestParam("prepTime") Integer prepTime,
            @RequestParam("cookTime") Integer cookTime,
            @RequestParam("difficulty") String difficulty,
            @RequestParam("instructions") String instructions,
            @RequestParam("ingredientName") List<String> ingredientNames,
            @RequestParam("quantity") List<String> quantities,
            @RequestParam("unit") List<String> units,
            @RequestParam("Category")  String category,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Principal principal
    ) {
        recipeService.saveRecipe(title, prepTime, cookTime, difficulty, instructions, ingredientNames, quantities, units, category, image, principal);
        return "redirect:/services";
    }


}
