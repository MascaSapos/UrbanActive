package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String showLogin() {
        // Thymeleaf buscará "login.html" dentro de src/main/resources/templates
        return "login";
    }
}
