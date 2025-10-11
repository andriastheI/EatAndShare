package edu.carroll.EatAndShare.web.controller;

import edu.carroll.EatAndShare.web.service.RecipeService;
import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.web.form.UserForm;
import edu.carroll.EatAndShare.web.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main controller for handling homepage, login, registration, and session management
 * in the EatAndShare web application.
 *
 * <p>This controller serves as the entry point for both guest and authenticated
 * users. It manages login validation, user registration, and logout logic, and
 * injects dynamic content (e.g., latest recipes) into the Thymeleaf views.</p>
 *
 * <p>Session attributes are used to persist login state and basic user info
 * between requests. When a user logs in successfully, their details are stored
 * in the session, allowing Thymeleaf templates to display personalized content.</p>
 *
 * <p>Views handled by this controller:</p>
 * <ul>
 *   <li><strong>index.html</strong> – Home page with login/register forms and recipe list.</li>
 *   <li><strong>services.html</strong> – Authenticated user page.</li>
 * </ul>
 *
 * @author Andrias and Selin
 * @version 1.0
 * @since 2025-10-11
 */
@Controller
public class IndexController {

    /** Service handling user authentication, registration, and lookup operations. */
    private final UserService userService;

    private final static Logger log = LoggerFactory.getLogger(IndexController.class);

    /** Service for retrieving and managing recipe data for the homepage. */
    @Autowired
    private RecipeService recipeService;

    /**
     * Constructor-based dependency injection for {@link UserService}.
     *
     * @param userService service handling user-related business logic
     */
    public IndexController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the main index page.
     *
     * <p>This method prepares login and registration forms and populates
     * model attributes with session data to maintain user state across
     * page loads. It also fetches and displays the latest recipes.</p>
     *
     * @param model the model to pass attributes to the Thymeleaf view
     * @param session the HTTP session containing login state
     * @return the name of the view to render ("index")
     */
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new User());
        }

        // Load login state from session
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        // Inject session values into the model
        model.addAttribute("loggedIn", loggedIn != null && loggedIn);
        model.addAttribute("username", username != null ? username : "");
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("firstName", firstName != null ? firstName : "");
        model.addAttribute("lastName", lastName != null ? lastName : "");

        // Add latest recipes to homepage
        model.addAttribute("recipes", recipeService.latestRecipes());
        log.info("Loading Homepage for the user: {}", username);

        return "index";
    }

    /**
     * Handles login form submission.
     *
     * <p>Validates credentials provided in the {@link UserForm}. If authentication
     * fails, the page reloads with an error message. On success, the user's
     * information is stored in the session and the user is redirected to the home page.</p>
     *
     * @param userForm contains username and password input
     * @param result binding result for validation errors
     * @param model the view model
     * @param session HTTP session for storing user data
     * @return redirect to home page or re-rendered login page
     */
    @PostMapping("/")
    public String loginPost(@ModelAttribute("userForm") UserForm userForm,
                            BindingResult result,
                            Model model,
                            HttpSession session) {

        if (!userService.validateUser(userForm)) {
            result.reject("login.invalid", "Username and password do not match known users");
            model.addAttribute("userForm", userForm);
            model.addAttribute("registerForm", new User());
            model.addAttribute("loggedIn", false);
            return "index";
        }

        User user = userService.findByUsername(userForm.getUsername());
        if (user == null) {
            result.reject("login.notfound", "User not found in database");
            model.addAttribute("userForm", userForm);
            model.addAttribute("registerForm", new User());
            model.addAttribute("loggedIn", false);
            return "index";
        }

        // ✅ Save user info in session so it persists across requests
        session.setAttribute("firstName", user.getFirstName());
        session.setAttribute("lastName", user.getLastName());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("loggedIn", true);

        return "redirect:/";
    }

    /**
     * Handles user registration requests.
     *
     * <p>Attempts to create a new user account using the provided registration form.
     * If the username or email already exists, an error message is displayed.
     * Upon successful registration, the login modal is displayed automatically.</p>
     *
     * @param user new user object populated from the registration form
     * @param model the view model
     * @param attrs redirect attributes for passing flash messages
     * @return redirect to the home page with login modal or error message
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerForm") User user,
                               Model model,
                               RedirectAttributes attrs) {
        try {
            userService.saveUser(user);

            // Show login modal after successful registration
            attrs.addFlashAttribute("showLogin", true);
            return "redirect:/";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Registration failed");
            model.addAttribute("userForm", new UserForm());
            model.addAttribute("registerForm", new User());
            model.addAttribute("showRegister", true);
            model.addAttribute("loggedIn", false);
            return "index";
        }
    }

    /**
     * Logs the user out by clearing the session and redirecting to the home page.
     *
     * @param session the current user session
     * @return redirect to home page after logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // ✅ Clear session data
        return "redirect:/";
    }

    /**
     * Displays the Services page for logged-in users.
     *
     * <p>If a user attempts to access this page without being logged in,
     * they are redirected back to the home page with the login popup displayed.</p>
     *
     * @param session the HTTP session to check user login state
     * @param model the model to populate session data for Thymeleaf templates
     * @param attrs redirect attributes for flashing messages
     * @return the "services" view if logged in, otherwise redirect to home
     */
    @GetMapping("/services")
    public String services(HttpSession session, Model model, RedirectAttributes attrs) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        if (loggedIn == null || !loggedIn) {
            // Not logged in → back to homepage with login popup
            attrs.addFlashAttribute("showLogin", true);
            return "redirect:/";
        }

        // Add session info to model so Thymeleaf can render header data
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("firstName", session.getAttribute("firstName"));
        model.addAttribute("lastName", session.getAttribute("lastName"));
        model.addAttribute("loggedIn", true);

        return "services"; // Loads services.html
    }
}
