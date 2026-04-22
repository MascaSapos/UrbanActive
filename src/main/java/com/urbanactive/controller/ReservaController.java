package com.urbanactive.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.urbanactive.model.Actividad;
import com.urbanactive.model.Reserva;
import com.urbanactive.model.Usuario;
import com.urbanactive.security.CustomUserDetails;
import com.urbanactive.service.ActividadService;
import com.urbanactive.service.ReservaService;
import com.urbanactive.service.UsuarioService;

@Controller
public class ReservaController {

    private final ReservaService reservaService;
    private final ActividadService actividadService;
    private final UsuarioService usuarioService;

    public ReservaController(ReservaService reservaService, ActividadService actividadService, UsuarioService usuarioService) {
        this.reservaService = reservaService;
        this.actividadService = actividadService;
        this.usuarioService = usuarioService;
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
    public String verMisReservas(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            Usuario usuario = usuarioService.obtenerPorEmail(auth.getName());
            model.addAttribute("usuario", usuario);
            String inicial = (usuario != null && usuario.getNombre() != null && !usuario.getNombre().isEmpty())
                    ? String.valueOf(usuario.getNombre().charAt(0)).toUpperCase()
                    : "?";
            model.addAttribute("avatarInicial", inicial);
        } catch (Exception e) {
            model.addAttribute("usuario", null);
            model.addAttribute("avatarInicial", "?");
        }
        return "mis-reservas";
    }
}
