package edu.carroll.eatAndShare.web.controller;

import edu.carroll.eatAndShare.web.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import edu.carroll.eatAndShare.backEnd.model.Recipe;

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
 * @author Andrias
 * @version 1.1
 * @since 2025-10-20
 */
@Controller
public class RecipeController {

    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    /** Service that manages recipe creation, storage, and retrieval. */
    private final RecipeService recipeService;
    private static final int MAX_PREP_MINS = 600;   // 10 hours
    private static final int MAX_COOK_MINS = 600;   // 10 hours

    /**
     * Constructor-based dependency injection.
     * This approach is preferred over field injection (@Autowired)
     * because it makes the dependency explicit, easier to test, and immutable.
     */
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Handles recipe submission requests.
     *
     * <p>This method receives recipe form data from the frontend (including
     * title, prep time, ingredients, category, and image). It validates that
     * the user is logged in before delegating the saving process to
     * {@link RecipeService#saveRecipe(String, Integer, Integer, String, String, List, List, List, String, MultipartFile, String)}.</p>
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
     * @return redirect to that category’s page, or services page on error
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

        // why: app uses session-based auth, Principal is null
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

            log.info("✅ User '{}' successfully uploaded recipe '{}'", username, title);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe added successfully!");

            String categoryPath = categoryName.trim().toLowerCase().replaceAll("\\s+", "");
            return "redirect:/" + categoryPath;
        } catch (IllegalArgumentException ex) {
        // ✅ Show ONLY the friendly message from the service
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/services";

    }
        catch (Exception e) {
            log.error("❌ Error while uploading recipe: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/services";
        }
    }

    @GetMapping("/recipes/{id}")
    public String getRecipe(@PathVariable Integer id, Model model, HttpSession session) {
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



