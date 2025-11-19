package edu.carroll.eatAndShare.web.controller;

import edu.carroll.eatAndShare.backEnd.model.Recipe;
import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.service.RecipeService;
import edu.carroll.eatAndShare.backEnd.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Filename: ProfileController.java
 * Author: Selin
 * Date: October 20, 2025
 *
 * Description:
 * Controller responsible for managing user profile operations, including:
 * - Displaying the logged-in user's profile page
 * - Displaying user-specific recipe listings
 * - Allowing users to delete their own recipes safely
 *
 * All routes in this controller are scoped under the "/profile" path and
 * require the user to be logged in (validated via session).
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    /** Service for retrieving and managing user information. */
    private final UserService userService;

    /** Service for retrieving and managing user-owned recipes. */
    private final RecipeService recipeService;

    /**
     * Constructor-based injection of required services.
     *
     * @param userService     service handling user lookup
     * @param recipeService   service for retrieving user recipes
     */
    public ProfileController(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }

    /**
     * Displays the logged-in user's profile page.
     *
     * <p>This method:
     * <ul>
     *   <li>Retrieves the user from the current session.</li>
     *   <li>If no user is found, redirects to the homepage.</li>
     *   <li>Loads the user's saved recipes from the database.</li>
     *   <li>Adds all necessary user and recipe data to the model for rendering.</li>
     * </ul>
     *
     * @param session the active HTTP session containing the logged-in user
     * @param model   the MVC model used to pass attributes to the view
     * @return the "profile" template or a redirect to "/"
     */
    @GetMapping
    public String viewProfile(HttpSession session, Model model) {

        // Retrieve the user from the session; redirect if not logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        // Load all recipes created by this user
        List<Recipe> recipes = recipeService.findByUser(user);

        // Populate model with user identity for header and profile display
        model.addAttribute("loggedIn", true);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("recipes", recipes);

        return "profile";
    }

    /**
     * Deletes a recipe that belongs to the currently logged-in user.
     *
     * <p>This method:
     * <ul>
     *   <li>Fetches the user from the session.</li>
     *   <li>Ensures the user exists before attempting deletion.</li>
     *   <li>Deletes the recipe only if it belongs to that user
     *       (enforced inside {@link RecipeService}).</li>
     *   <li>Redirects back to the profile page after deletion.</li>
     * </ul>
     *
     * @param id      ID of the recipe to delete
     * @param session the current HTTP session holding user data
     * @return redirect back to "/profile"
     */
    @PostMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable Integer id, HttpSession session) {

        // Retrieve user from session; only delete if user is logged in
        User user = (User) session.getAttribute("user");
        if (user != null) {
            recipeService.deleteRecipeByIdAndUser(id, user);
        }

        return "redirect:/profile";
    }
}
