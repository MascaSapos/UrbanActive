package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.urbanactive.service.ActividadService;

@Controller
public class WebController {

    private final ActividadService actividadService;

    public WebController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping("/login")
    public String showLogin(
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "login";
    }

    @GetMapping("/")
    public String showIndex(Model model) {
        model.addAttribute("actividades", actividadService.obtenerTodas());
        return "index";
    }
}
