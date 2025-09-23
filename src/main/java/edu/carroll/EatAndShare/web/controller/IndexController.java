package edu.carroll.EatAndShare.web.controller;

import edu.carroll.EatAndShare.web.form.LoginForm;
import edu.carroll.EatAndShare.web.form.RegisterForm;
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
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "index";
    }


    private final LoginService loginService;
    public IndexController(LoginService loginService) {
        this.loginService = loginService;
    }


    @PostMapping("/")
    public String loginPost(@Validated @ModelAttribute LoginForm loginForm, BindingResult result, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return "index";
        }

        if (!loginService.validateUser(loginForm)) {
            result.addError(new ObjectError("globalError", "Username and password do not match known users"));
            return "index";
        }
        attrs.addAttribute("username", loginForm.getUsername());
        return "redirect:/loginSuccess";
    }

    @PostMapping("/")
    public String registerPost(@Validated @ModelAttribute RegisterForm registerForm, BindingResult result, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return "index";
        }

        if (!loginService.validateUser(registerForm)) {
            result.addError(new ObjectError("globalError", "Username and password do not match known users"));
            return "index";
        }
        attrs.addAttribute("username", registerForm.getUsername());
        return "redirect:/loginSuccess";
    }
    

    @GetMapping("/loginSuccess")
    public String loginSuccess(String username, Model model) {
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