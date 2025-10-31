package edu.carroll.EatAndShare.web.controller;

import edu.carroll.EatAndShare.web.service.RecipeService;
import edu.carroll.EatAndShare.web.service.UserService;
import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.backEnd.model.Recipe;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final RecipeService recipeService;

    public ProfileController(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }

    // === PROFILE PAGE ===
    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        List<Recipe> recipes = recipeService.findByUser(user);

        model.addAttribute("loggedIn", true);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("recipes", recipes);

        return "profile";
    }

    // === DELETE RECIPE ===
    @PostMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable Integer id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            recipeService.deleteRecipeByIdAndUser(id, user);
        }
        return "redirect:/profile";
    }
}
