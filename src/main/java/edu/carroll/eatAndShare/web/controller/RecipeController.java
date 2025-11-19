package edu.carroll.eatAndShare.web.controller;

import edu.carroll.eatAndShare.backEnd.model.Recipe;
import edu.carroll.eatAndShare.backEnd.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Filename: RecipeController.java
 * Author: Andrias and Selin
 * Date: October 20, 2025
 *
 * Description:
 * Controller responsible for handling all recipe-related web requests, including:
 * - Submitting new recipes (title, times, difficulty, instructions, category, image)
 * - Linking ingredients to recipes
 * - Viewing individual recipe details by ID
 *
 * The controller delegates business logic to {@link RecipeService} and ensures
 * users are authenticated before submitting recipes.
 */
@Controller
public class RecipeController {

    /** Logger for tracking recipe uploads and errors. */
    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    /** Service responsible for recipe creation, storage, and retrieval. */
    private final RecipeService recipeService;

    /**
     * Constructor-based dependency injection.
     *
     * @param recipeService service handling recipe persistence and retrieval
     */
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Handles recipe submission from the Services page.
     *
     * <p>This method:
     * <ul>
     *   <li>Validates login state via session.</li>
     *   <li>Collects all recipe information from the form.</li>
     *   <li>Delegates saving to {@link RecipeService#saveRecipe}.</li>
     *   <li>Redirects the user to the correct category page after success.</li>
     * </ul>
     *
     * @param title            recipe title
     * @param prepTime         recipe preparation time in minutes
     * @param cookTime         cooking time in minutes
     * @param difficulty       difficulty level (Easy / Medium / Hard)
     * @param instructions     full cooking instructions
     * @param ingredientNames  list of ingredient names
     * @param quantities       list of ingredient quantities
     * @param units            list of ingredient units
     * @param categoryName     category this recipe belongs to
     * @param image            optional recipe image
     * @param session          session used for login validation
     * @param redirectAttributes flash attributes for feedback messages
     * @return redirect to the appropriate category page or services page on error
     */
    @PostMapping("/recipes/add")
    public String uploadRecipe(@RequestParam String title,
                               @RequestParam Integer prepTime,
                               @RequestParam Integer cookTime,
                               @RequestParam String difficulty,
                               @RequestParam String instructions,
                               @RequestParam List<String> ingredientNames,
                               @RequestParam List<String> quantities,
                               @RequestParam List<String> units,
                               @RequestParam String categoryName,
                               @RequestParam("image") MultipartFile image,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        // Ensure user is logged in
        if (!Boolean.TRUE.equals(session.getAttribute("loggedIn"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to submit a recipe.");
            return "redirect:/";
        }

        final String username = String.valueOf(session.getAttribute("username"));

        try {
            recipeService.saveRecipe(
                    title, prepTime, cookTime, difficulty, instructions,
                    ingredientNames, quantities, units, categoryName, image, username
            );

            log.info("Recipe '{}' uploaded successfully by user '{}'", title, username);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe added successfully!");

            // Build a clean URL path from category name (e.g., "Salad" → "/salad")
            String categoryPath = categoryName.trim().toLowerCase().replaceAll("\\s+", "");
            return "redirect:/" + categoryPath;

        } catch (IllegalArgumentException ex) {

            // Friendly validation message from the service
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/services";

        } catch (Exception e) {

            // Unexpected internal error
            log.error("Error while uploading recipe: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/services";
        }
    }

    /**
     * Displays the details page for a specific recipe.
     *
     * <p>This method:
     * <ul>
     *   <li>Fetches a recipe using its ID.</li>
     *   <li>Adds recipe data and session-based user info to the view model.</li>
     *   <li>Renders the recipeDetails page.</li>
     * </ul>
     *
     * <p>If the recipe does not exist, {@link RecipeService#getRecipe(Integer)} will
     * throw an exception which is expected to be handled globally.</p>
     *
     * @param id      ID of the recipe to display
     * @param model   MVC model used to pass data to the view
     * @param session active HTTP session used to determine login state
     * @return the recipeDetails template
     */
    @GetMapping("/recipes/{id}")
    public String getRecipe(@PathVariable Integer id,
                            Model model,
                            HttpSession session) {

        Recipe recipe = recipeService.getRecipe(id);

        model.addAttribute("recipe", recipe);
        model.addAttribute("loggedIn", Boolean.TRUE.equals(session.getAttribute("loggedIn")));
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("firstName", session.getAttribute("firstName"));
        model.addAttribute("lastName", session.getAttribute("lastName"));

        return "recipeDetails";
    }
}
