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

@Controller
public class IndexController {

    private final LoginService loginService;

    public IndexController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new Login());
        }

        if (!model.containsAttribute("loggedIn")) {
            model.addAttribute("loggedIn", Boolean.FALSE);
        }
        if (!model.containsAttribute("username")) {
            model.addAttribute("username", "");
        }
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }

        return "index";
    }


    @PostMapping("/")
    public String loginPost(@ModelAttribute("loginForm") LoginForm loginForm,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attrs) {

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


        attrs.addFlashAttribute("username", user.getUsername());
        attrs.addFlashAttribute("email", user.getEmail());
        attrs.addFlashAttribute("loggedIn", true);

        return "redirect:/";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerForm") Login login,
                               Model model,
                               RedirectAttributes attrs) {
        try {
            loginService.saveUser(login);


            attrs.addFlashAttribute("showLogin", true);
            attrs.addFlashAttribute("loggedIn", false);

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


    // new logout mapping
    @GetMapping("/logout")
    public String logout(RedirectAttributes attrs) {
        attrs.addFlashAttribute("loggedIn", false);
        attrs.addFlashAttribute("username", "");
        attrs.addFlashAttribute("email", "");
        return "redirect:/";
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }
}
