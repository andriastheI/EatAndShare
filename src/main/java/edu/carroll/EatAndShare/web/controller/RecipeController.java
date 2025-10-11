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

/**
 * Controller responsible for handling all recipe-related web requests.
 *
 * <p>This includes recipe submission (with image upload, category assignment,
 * and ingredient linking) and recipe viewing. It interacts with the
 * {@link RecipeService} to perform all business logic while keeping
 * the controller focused on request handling and session validation.</p>
 *
 * <p>Routes handled by this controller:</p>
 * <ul>
 *   <li><strong>POST /recipes/add</strong> — for adding new recipes.</li>
 *   <li><strong>GET /recipes/{id}</strong> — for viewing recipe details.</li>
 * </ul>
 *
 * <p>All uploads are validated through user session data to ensure
 * that only authenticated users can submit recipes.</p>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Controller
public class RecipeController {

    /** Service that manages recipe creation, storage, and retrieval. */
    @Autowired
    private RecipeService recipeService;

    /**
     * Handles recipe submission requests.
     *
     * <p>This method receives recipe form data from the frontend (including
     * title, prep time, ingredients, category, and image). It validates that
     * the user is logged in before delegating the saving process to
     * {@link RecipeService#saveRecipe(String, Integer, Integer, String, String, List, List, List, String, MultipartFile, String)}.</p>
     *
     * <p>Upon success, a confirmation message is displayed on the services page.
     * On failure, an error message is shown instead.</p>
     *
     * @param title the recipe title
     * @param prepTime preparation time in minutes
     * @param cookTime cooking time in minutes
     * @param difficulty recipe difficulty level
     * @param instructions step-by-step cooking instructions
     * @param ingredientNames list of ingredient names
     * @param quantities list of ingredient quantities
     * @param units list of ingredient units
     * @param categoryName recipe category name
     * @param image uploaded recipe image (optional)
     * @param session current user session (used for login validation)
     * @param redirectAttributes used to display flash messages after redirect
     * @return a redirect to the services page or home page with appropriate message
     */
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
            // ✅ Retrieve user session info
            String username = (String) session.getAttribute("username");
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

            // ✅ Prevent unauthorized recipe submissions
            if (loggedIn == null || !loggedIn || username == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "⚠️ You must log in before submitting a recipe.");
                return "redirect:/";
            }

            // ✅ Save recipe and all related data
            recipeService.saveRecipe(title, prepTime, cookTime, difficulty, instructions,
                    ingredientNames, quantities, units, categoryName, image, username);

            redirectAttributes.addFlashAttribute("successMessage", "✅ Recipe saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Failed to save recipe: " + e.getMessage());
        }

        return "redirect:/services";
    }

    /**
     * Displays a single recipe's details by its ID.
     *
     * <p>This method retrieves a recipe and its linked data (user, category,
     * ingredients) and sends it to the view template for rendering.</p>
     *
     * <p>If the user is logged in, their session details are included in the
     * model for personalized display.</p>
     *
     * @param id recipe ID to retrieve
     * @param model used to pass recipe and session data to the view
     * @param session current HTTP session
     * @return the name of the Thymeleaf template to render (recipeDetails.html)
     * @throws IllegalArgumentException if recipe is not found
     */
    @GetMapping("/recipes/{id}")
    public String viewRecipe(@PathVariable Integer id, Model model, HttpSession session) {
        Recipe recipe = recipeService.getRecipeOrThrow(id);

        model.addAttribute("recipe", recipe);
        model.addAttribute("loggedIn", Boolean.TRUE.equals(session.getAttribute("loggedIn")));
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("firstName", session.getAttribute("firstName"));
        model.addAttribute("lastName", session.getAttribute("lastName"));

        return "recipeDetails"; // Loads recipeDetails.html view
    }
}
