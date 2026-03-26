package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.urbanactive.service.ActividadService;

@Controller
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping("/leaflet")
    public String verActividadesLeaflet(Model model) {
        model.addAttribute("actividades", actividadService.obtenerTodas());
        return "leaflet";
    }
}

