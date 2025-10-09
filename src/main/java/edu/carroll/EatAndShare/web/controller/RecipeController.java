package edu.carroll.EatAndShare.web.controller;

import edu.carroll.EatAndShare.web.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import edu.carroll.EatAndShare.backEnd.model.Recipe;

import java.util.List;

@Controller
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @PostMapping("/recipes/add")
    public String uploadRecipe(
            @RequestParam("title") String title,
            @RequestParam("prepTime") Integer prepTime,
            @RequestParam("cookTime") Integer cookTime,
            @RequestParam("difficulty") String difficulty,
            @RequestParam("instructions") String instructions,
            @RequestParam("ingredientName[]") List<String> ingredientNames,
            @RequestParam("quantity[]") List<String> quantities,
            @RequestParam("unit[]") List<String> units,
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // ✅ Grab user info from session
            String username = (String) session.getAttribute("username");
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

            if (loggedIn == null || !loggedIn || username == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "⚠️ You must log in before submitting a recipe.");
                return "redirect:/";
            }

            recipeService.saveRecipe(title, prepTime, cookTime, difficulty, instructions,
                    ingredientNames, quantities, units, categoryName, image, username);

            redirectAttributes.addFlashAttribute("successMessage", "✅ Recipe saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Failed to save recipe: " + e.getMessage());
        }

        return "redirect:/services";
    }

    @GetMapping("/recipes/{id}")
    public String viewRecipe(@PathVariable Integer id, Model model, HttpSession session) {
        Recipe recipe = recipeService.getRecipeOrThrow(id);

        model.addAttribute("recipe", recipe);
        model.addAttribute("loggedIn", Boolean.TRUE.equals(session.getAttribute("loggedIn")));
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("firstName", session.getAttribute("firstName"));
        model.addAttribute("lastName", session.getAttribute("lastName"));

        return "recipeDetails"; // new Thymeleaf template
    }

}
