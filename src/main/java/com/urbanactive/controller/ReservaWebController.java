package com.urbanactive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.urbanactive.service.ReservaService;

/**
 * Controlador web para las operaciones de reserva desde el frontend Thymeleaf.
 */
@Controller
public class ReservaWebController {

    private final ReservaService reservaService;

    public ReservaWebController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * POST /reservas — el usuario selecciona una actividad en el sidebar y pulsa
     * "Reservar plaza". El formulario envía el id de la actividad y Spring Security
     * proporciona el usuario autenticado mediante el objeto Authentication.
     */
    @PostMapping("/reservas")
    public String reservar(
            @RequestParam("actividadId") String actividadId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName(); // Spring Security devuelve el username (email)
        try {
            reservaService.crearParaUsuario(actividadId, email);
            redirectAttributes.addFlashAttribute("reservaExito", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("reservaError", e.getMessage());
        }
        return "redirect:/";
    }
}
