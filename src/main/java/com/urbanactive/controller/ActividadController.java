package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.urbanactive.service.ActividadService;

@Controller
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping("/")
    public String verActividadesLeaflet(Model model) {
        model.addAttribute("actividades", actividadService.obtenerTodas());
        return "index";
    }

    @GetMapping("/actividades/nueva")
    public String nuevaActividadForm(Model model) {
        // model.addAttribute("actividad", new Actividad());
        return "nueva-actividad";
    }

    @PostMapping("/actividades/nueva")
    public String guardarActividad(com.urbanactive.model.Actividad actividad) {
        actividad.setId(java.util.UUID.randomUUID().toString().substring(0, 10)); // o logic id
        actividadService.crear(actividad);
        return "redirect:/";
    }
}

