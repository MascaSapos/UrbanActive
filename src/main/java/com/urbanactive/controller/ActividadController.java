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
    private final com.urbanactive.repository.UbicacionRepository ubicacionRepository;

    public ActividadController(ActividadService actividadService, 
                               com.urbanactive.service.ReservaService reservaService,
                               com.urbanactive.repository.UbicacionRepository ubicacionRepository) {
        this.actividadService = actividadService;
        this.reservaService = reservaService;
        this.ubicacionRepository = ubicacionRepository;
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
        model.addAttribute("ubicaciones", ubicacionRepository.findAll());
        return "nueva-actividad";
    }

    @PostMapping("/actividades/nueva")
    public String guardarActividad(com.urbanactive.model.Actividad actividad, 
                                   @org.springframework.web.bind.annotation.RequestParam("lat") Double lat,
                                   @org.springframework.web.bind.annotation.RequestParam("lng") Double lng,
                                   @org.springframework.web.bind.annotation.RequestParam("nombreUbicacion") String nombreUbicacion,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "tipoEspacio", defaultValue = "EXTERIOR") String tipoEspacio,
                                   @org.springframework.security.core.annotation.AuthenticationPrincipal com.urbanactive.security.CustomUserDetails userDetails) {
        
        // Crear nueva ubicación desde los datos del mapa
        com.urbanactive.model.Ubicacion u = new com.urbanactive.model.Ubicacion();
        u.setId("LOC-" + java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        u.setLatitud(java.math.BigDecimal.valueOf(lat));
        u.setLongitud(java.math.BigDecimal.valueOf(lng));
        u.setNombre(nombreUbicacion);
        u.setTipoEspacio(tipoEspacio);
        
        ubicacionRepository.save(u);
        actividad.setId_ubicacion(u);

        actividad.setId(java.util.UUID.randomUUID().toString().substring(0, 10));
        actividad.setEstado("ABIERTA");
        
        if (userDetails != null) {
            actividad.setOrganizador(userDetails.getUsuario());
            actividad.setOrganizadorEmail(userDetails.getUsuario().getEmail());
        }
        
        actividadService.crear(actividad);
        return "redirect:/perfil-organizador?newActId=" + actividad.getId();
    }
}
