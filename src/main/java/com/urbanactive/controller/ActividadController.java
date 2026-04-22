package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.urbanactive.service.ActividadService;

@Controller
public class ActividadController {

    private final ActividadService actividadService;
    private final com.urbanactive.service.ReservaService reservaService;

    public ActividadController(ActividadService actividadService, com.urbanactive.service.ReservaService reservaService) {
        this.actividadService = actividadService;
        this.reservaService = reservaService;
    }

    @GetMapping("/actividades/mapa")
    public String verActividadesLeaflet(Model model) {
        model.addAttribute("actividades", actividadService.obtenerTodas());
        return "index";
    }

    @GetMapping("/actividades/{id}")
    public String verDetallesActividad(@PathVariable("id") String id, 
                                       Model model, 
                                       @org.springframework.security.core.annotation.AuthenticationPrincipal com.urbanactive.security.CustomUserDetails userDetails) {
        model.addAttribute("actividad", actividadService.obtenerConDetalles(id));
        boolean yaInscrito = false;
        if (userDetails != null) {
            yaInscrito = reservaService.estaUsuarioInscrito(id, userDetails.getUsername());
        }
        model.addAttribute("yaInscrito", yaInscrito);
        return "detalles-actividad";
    }

    @GetMapping("/actividades/nueva")
    public String nuevaActividadForm(Model model) {
        // model.addAttribute("actividad", new Actividad());
        return "nueva-actividad";
    }

    @PostMapping("/actividades/nueva")
    public String guardarActividad(com.urbanactive.model.Actividad actividad, 
                                   @org.springframework.security.core.annotation.AuthenticationPrincipal com.urbanactive.security.CustomUserDetails userDetails) {
        actividad.setId(java.util.UUID.randomUUID().toString().substring(0, 10));
        if (userDetails != null) {
            actividad.setOrganizador(userDetails.getUsuario());
            actividad.setOrganizadorEmail(userDetails.getUsuario().getEmail());
        }
        actividadService.crear(actividad);
        return "redirect:/";
    }
}
