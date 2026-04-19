package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;

import com.urbanactive.service.ActividadService;

@Controller
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping("/actividades/mapa")
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
    public String guardarActividad(com.urbanactive.model.Actividad actividad, Authentication auth) {
        actividad.setId(java.util.UUID.randomUUID().toString().substring(0, 10));
        if (auth != null && auth.isAuthenticated()) {
            actividad.setOrganizadorEmail(auth.getName());
        }
        actividadService.crear(actividad);
        return "redirect:/";
    }
}
