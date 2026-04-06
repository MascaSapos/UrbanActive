package com.urbanactive.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.urbanactive.model.Actividad;
import com.urbanactive.model.Reserva;
import com.urbanactive.security.CustomUserDetails;
import com.urbanactive.service.ActividadService;
import com.urbanactive.service.ReservaService;

@Controller
public class ReservaController {

    private final ReservaService reservaService;
    private final ActividadService actividadService;

    public ReservaController(ReservaService reservaService, ActividadService actividadService) {
        this.reservaService = reservaService;
        this.actividadService = actividadService;
    }

    @PostMapping("/actividades/{id}/reservar")
    public String reservarActividad(@PathVariable("id") String actividadId, 
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            Actividad actividad = actividadService.obtenerPorId(actividadId);
            Reserva reserva = new Reserva();
            reserva.setActividad(actividad);
            reserva.setUsuario(userDetails.getUsuario());
            
            reservaService.crear(reserva);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Reserva completada con éxito!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/mis-reservas")
    public String verMisReservas(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("reservas", reservaService.obtenerPorUsuario(userDetails.getUsuario()));
        return "mis-reservas";
    }
}
