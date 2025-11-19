package edu.carroll.eatAndShare.web.controller;

import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.service.RecipeService;
import edu.carroll.eatAndShare.backEnd.service.UserService;
import edu.carroll.eatAndShare.backEnd.form.UserForm;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Filename: IndexController.java
 * Author: Andrias and Selin
 * Date: October 20, 2025
 *
 * Description:
 * Main MVC controller responsible for:
 * - Homepage logic (with search + pagination support)
 * - User login and registration
 * - Session management (store & clear user info)
 * - Category pages and static pages (about/contact)
 *
 * This controller acts as the primary entry point for public and authenticated
 * traffic in the EatAndShare application.
 */
@Controller
public class IndexController {

    /**
     * Logger for debugging and request lifecycle tracing.
     */
    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    /**
     * Service for user authentication and data retrieval.
     */
    private final UserService userService;

    /**
     * Service for retrieving recipe data for homepage & categories.
     */
    private final RecipeService recipeService;

    /**
     * Constructor-based injection of required services.
     *
     * @param userService   the user service used for authentication + registration
     * @param recipeService the recipe service for listing and searching recipes
     */
    public IndexController(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }

    /**
     * Displays the homepage with optional search functionality.
     *
     * @param q       search query (optional)
     * @param page    current page index
     * @param size    number of elements per page
     * @param model   MVC model for view rendering
     * @param session HTTP session used to persist login state
     * @return the index view
     */
    @GetMapping("/")
    public String index(@RequestParam(value = "q", required = false) String q,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "12") int size,
                        Model model,
                        HttpSession session) {

        // Create login/registration forms on initial load
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new User());
        }

        // Populate login/session data into model
        populateLoginState(model, session);

        boolean searching = q != null && !q.isBlank();
        model.addAttribute("searching", searching);
        model.addAttribute("q", q);

        // If no search → show empty state (homepage only)
        if (!searching) {
            model.addAttribute("recipes", java.util.Collections.emptyList());
            model.addAttribute("resultCount", 0L);
            model.addAttribute("page", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("size", size);
            return "index";
        }

        // Build pageable (validated for proper bounds)
        if (page < 0) page = 0;
        if (size < 1 || size > 60) size = 12;

        var pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("id").descending()
        );

        // Perform search (case‐insensitive)
        var recipesPage = recipeService.searchRecipes(q.trim(), pageable);

        // Populate model for rendering
        model.addAttribute("recipesPage", recipesPage);
        model.addAttribute("recipes", recipesPage.getContent());
        model.addAttribute("resultCount", recipesPage.getTotalElements());
        model.addAttribute("page", recipesPage.getNumber());
        model.addAttribute("totalPages", recipesPage.getTotalPages());
        model.addAttribute("size", recipesPage.getSize());

        return "index";
    }

    /**
     * Handles login form submission and user credential validation.
     *
     * @param userForm user-entered credentials
     * @param result   validation errors holder
     * @param model    MVC model
     * @param session  session storing user identity
     * @return redirect or failed login view
     */
    @PostMapping("/")
    public String loginPost(@ModelAttribute("userForm") UserForm userForm,
                            BindingResult result,
                            Model model,
                            HttpSession session) {

        // Validate credentials (not storing password in logs)
        if (!userService.validateUser(userForm)) {
            result.reject("login.invalid", "Username and password do not match known users");
            model.addAttribute("userForm", userForm);
            model.addAttribute("registerForm", new User());
            model.addAttribute("loggedIn", false);
            return "index";
        }

        // Retrieve user profile
        User user = userService.findByUsername(userForm.getUsername());
        if (user == null) {
            result.reject("login.notfound", "User not found in database");
            model.addAttribute("loggedIn", false);
            return "index";
        }

        // Store user identity in session
        session.setAttribute("firstName", user.getFirstName());
        session.setAttribute("lastName", user.getLastName());
        session.setAttribute("username", user.getUsername());  // used for auth checks
        session.setAttribute("email", user.getEmail());
        session.setAttribute("loggedIn", true);
        session.setAttribute("user", user);

        log.info("Login successful — username='{}'", user.getUsername());

        return "redirect:/";
    }

    /**
     * Registers a new user, validates uniqueness, and handles errors.
     *
     * @param user  user data from registration form
     * @param model model for rendering view
     * @param attrs redirect flash attributes
     * @return redirect back to homepage
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerForm") User user,
                               Model model,
                               RedirectAttributes attrs) {

        try {
            userService.saveUser(user);
            attrs.addFlashAttribute("showLogin", true); // Show login modal automatically
            return "redirect:/";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {

            // On validation or DB failure → show registration modal again
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Registration failed");
            model.addAttribute("userForm", new UserForm());
            model.addAttribute("registerForm", new User());
            model.addAttribute("showRegister", true);
            model.addAttribute("loggedIn", false);
            return "index";
        }
    }

    /**
     * Logs the user out by clearing the current HTTP session.
     *
     * @param session active session
     * @return redirect to homepage
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        // Capture username before invalidating session
        String username = (String) session.getAttribute("username");

        // Log logout activity
        log.info("User '{}' logged out successfully", username);

        session.invalidate(); // Clear session data
        return "redirect:/";
    }

    /**
     * Displays the Services page for authenticated users only.
     *
     * @return services page or redirect
     */
    @GetMapping("/services")
    public String services(HttpSession session, Model model, RedirectAttributes attrs) {

        Boolean logged = (Boolean) session.getAttribute("loggedIn");
        if (logged == null || !logged) {
            attrs.addFlashAttribute("showLogin", true);
            return "redirect:/";
        }

        // Populate login/session data into model
        populateLoginState(model, session);
        return "services";
    }

    @GetMapping("/password")
    public String passwords(HttpSession session, Model model, RedirectAttributes attrs) {

        Boolean logged = (Boolean) session.getAttribute("loggedIn");
        if (logged == null || !logged) {
            attrs.addFlashAttribute("showLogin", true);
            return "redirect:/";
        }

        // Populate login/session data into model
        populateLoginState(model, session);
        return "password";
    }

    /**
     * Handles password update requests.
     * Validates old password and checks new password confirmation.
     */
    @PostMapping("/password")
    public String updatePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes attrs) {

        String username = (String) session.getAttribute("username");

        Boolean logged = (Boolean) session.getAttribute("loggedIn");
        if (logged == null || !logged) {
            attrs.addFlashAttribute("showLogin", true);
            return "redirect:/";
        }

        // Confirm new-password match
        if (!newPassword.equals(confirmPassword)) {
            attrs.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/password";
        }

        try {
            boolean updated = userService.updatePassword(username, oldPassword, newPassword);
            if (!updated) {
                attrs.addFlashAttribute("error", "Old password is incorrect.");
                return "redirect:/password";
            }

            attrs.addFlashAttribute("success", "Password updated successfully!");
            return "redirect:/password";

        } catch (Exception e) {
            attrs.addFlashAttribute("error", "Unexpected error, please try again.");
            return "redirect:/password";
        }
    }

    /**
     * Load recipes for Breakfast category.
     */
    @GetMapping("/breakfast")
    public String showBreakfast(Model model, HttpSession session) {
        populateLoginState(model, session);
        model.addAttribute("recipes", recipeService.findByCategoryName("Breakfast"));
        return "breakfast";
    }

    /**
     * Load recipes for Lunch category.
     */
    @GetMapping("/lunch")
    public String showLunch(Model model, HttpSession session) {
        populateLoginState(model, session);
        model.addAttribute("recipes", recipeService.findByCategoryName("Lunch"));
        return "lunch";
    }

    /**
     * Load recipes for Dinner category.
     */
    @GetMapping("/dinner")
    public String showDinner(Model model, HttpSession session) {
        populateLoginState(model, session);
        model.addAttribute("recipes", recipeService.findByCategoryName("Dinner"));
        return "dinner";
    }

    /**
     * Load recipes for Salad category.
     */
    @GetMapping("/salad")
    public String showSalad(Model model, HttpSession session) {
        populateLoginState(model, session);
        model.addAttribute("recipes", recipeService.findByCategoryName("Salad"));
        return "salad";
    }

    /**
     * Load recipes for Dessert category.
     */
    @GetMapping("/dessert")
    public String showDessert(Model model, HttpSession session) {
        populateLoginState(model, session);
        model.addAttribute("recipes", recipeService.findByCategoryName("Dessert"));
        return "dessert";
    }

    /**
     * Helper method to populate session → model attributes for every page.
     */
    private void populateLoginState(Model model, HttpSession session) {
        Boolean logged = (Boolean) session.getAttribute("loggedIn");
        boolean safeLogged = logged != null && logged;
        model.addAttribute("loggedIn", safeLogged);
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("firstName", session.getAttribute("firstName"));
        model.addAttribute("lastName", session.getAttribute("lastName"));
    }

    /**
     * Displays the About page.
     */
    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        // Load login/user info into the model for header rendering
        populateLoginState(model, session);
        return "about";
    }

    /**
     * Displays the Contact page.
     */
    @GetMapping("/contact")
    public String contactPage(Model model, HttpSession session) {
        // Add session info (username, loggedIn, etc.) for the contact page header
        populateLoginState(model, session);
        return "contact";
    }
}
