package edu.carroll.EatAndShare.web.controller;

import edu.carroll.EatAndShare.backEnd.model.Login;
import edu.carroll.EatAndShare.web.form.LoginForm;
import edu.carroll.EatAndShare.web.service.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {

    private final LoginService loginService;

    public IndexController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // always add both forms so Thymeleaf has them
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new Login());
        }
        return "index";
    }

    @PostMapping("/")
    public String loginPost(@Validated @ModelAttribute("loginForm") LoginForm loginForm,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attrs) {
        if (result.hasErrors()) {
            // repopulate register form so Thymeleaf won't break
            model.addAttribute("registerForm", new Login());
            return "index";
        }

        if (!loginService.validateUser(loginForm)) {
            result.addError(new ObjectError("globalError", "Username and password do not match known users"));
            model.addAttribute("registerForm", new Login()); // required for template
            return "index";
        }

        // put username in flash attributes so it survives redirect
        attrs.addFlashAttribute("username", loginForm.getUsername());
        return "redirect:/loginSuccess";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerForm") Login login,
                               Model model,
                               RedirectAttributes attrs) {
        try {
            loginService.saveUser(login);
            attrs.addFlashAttribute("message", "Registration successful. Please log in.");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loginForm", new LoginForm());
            model.addAttribute("registerForm", new Login());
            model.addAttribute("showRegister", true); // to reopen registration popup
            return "index";
        }
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(@ModelAttribute("username") String username, Model model) {
        model.addAttribute("username", username);
        return "loginSuccess";
    }

    @GetMapping("/loginFailure")
    public String loginFailure() {
        return "loginFailure";
    }

    @GetMapping("/services")
    public String services() {
        return "services";  // loads services.html
    }
}
