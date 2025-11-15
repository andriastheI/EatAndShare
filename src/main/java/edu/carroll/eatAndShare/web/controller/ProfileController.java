package edu.carroll.eatAndShare.web.controller;

import edu.carroll.eatAndShare.backEnd.service.RecipeService;
import edu.carroll.eatAndShare.backEnd.service.UserService;
import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.model.Recipe;
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

    /**
     * Displays the profile page for the currently logged-in user.
     * <p>
     * This method:
     * <ul>
     *     <li>Checks if a user is present in the HTTP session.</li>
     *     <li>If no user is found, redirects to the home page.</li>
     *     <li>If a user is found, loads their recipes and basic profile data,</li>
     *     <li>Adds all necessary attributes to the {@link Model} for rendering the profile view.</li>
     * </ul>
     *
     * @param session the current HTTP session containing the logged-in user
     * @param model   the model used to pass data to the view template
     * @return the name of the profile view template or a redirect to the home page
     */

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

    /**
     * Handles deletion of a recipe owned by the currently logged-in user.
     * <p>
     * This method:
     * <ul>
     *     <li>Retrieves the current user from the session.</li>
     *     <li>If the user is present, attempts to delete the recipe with the given ID
     *         scoped to that user (so users cannot delete others' recipes).</li>
     *     <li>Redirects back to the profile page after the operation.</li>
     * </ul>
     *
     * @param id      the ID of the recipe to delete
     * @param session the current HTTP session containing the logged-in user
     * @return a redirect string back to the profile page
     */
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
