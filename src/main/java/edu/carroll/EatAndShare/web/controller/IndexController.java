package edu.carroll.EatAndShare.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/services")
    public String services() {
        return "services";  // loads services.html
    }
}