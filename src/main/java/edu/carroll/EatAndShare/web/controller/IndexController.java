package edu.carroll.EatAndShare.web.controller;

import edu.carroll.EatAndShare.backEnd.model.Login;
import edu.carroll.EatAndShare.web.form.LoginForm;
import edu.carroll.EatAndShare.web.service.LoginService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {

    private final LoginService loginService;

    public IndexController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new Login());
        }

        // Load login state from session
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        model.addAttribute("loggedIn", loggedIn != null && loggedIn);
        model.addAttribute("username", username != null ? username : "");
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("firstName", firstName != null ? firstName : "");
        model.addAttribute("lastName", lastName != null ? lastName : "");

        return "index";
    }

    @PostMapping("/")
    public String loginPost(@ModelAttribute("loginForm") LoginForm loginForm,
                            BindingResult result,
                            Model model,
                            HttpSession session) {

        if (!loginService.validateUser(loginForm)) {
            result.reject("login.invalid", "Username and password do not match known users");
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("registerForm", new Login());
            model.addAttribute("loggedIn", false);
            return "index";
        }

        Login user = loginService.findByUsername(loginForm.getUsername());
        if (user == null) {
            result.reject("login.notfound", "User not found in database");
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("registerForm", new Login());
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

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerForm") Login login,
                               Model model,
                               RedirectAttributes attrs) {
        try {
            loginService.saveUser(login);

            attrs.addFlashAttribute("showLogin", true);
            return "redirect:/";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Registration failed");
            model.addAttribute("loginForm", new LoginForm());
            model.addAttribute("registerForm", new Login());
            model.addAttribute("showRegister", true);
            model.addAttribute("loggedIn", false);
            return "index";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // ✅ Clear session
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/services")
    public String services(HttpSession session, Model model, RedirectAttributes attrs) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        if (loggedIn == null || !loggedIn) {
            // Not logged in → back to homepage with login popup
            attrs.addFlashAttribute("showLogin", true);
            return "redirect:/";
        }

        // Add session info to model so Thymeleaf can render header
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("firstName", session.getAttribute("firstName"));
        model.addAttribute("lastName", session.getAttribute("lastName"));
        model.addAttribute("loggedIn", true);

        return "services"; // loads services.html
    }
}
