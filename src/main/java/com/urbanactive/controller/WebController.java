package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import com.urbanactive.service.ActividadService;
import com.urbanactive.service.UsuarioService;
import com.urbanactive.model.Usuario;

import com.urbanactive.repository.ReservaRepository;
import com.urbanactive.model.Actividad;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class WebController {

    private final ActividadService actividadService;
    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;

    public WebController(ActividadService actividadService, ReservaRepository reservaRepository, UsuarioService usuarioService) {
        this.actividadService = actividadService;
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
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
        List<Actividad> actividades = actividadService.obtenerTodas();
        for (Actividad a : actividades) {
            a.setPlazasOcupadas(reservaRepository.countActivasPorActividad(a.getId()));
        }
        model.addAttribute("actividades", actividades);
        return "index";
    }

    @GetMapping("/perfil-organizador")
    public String perfilOrganizador(Model model, Authentication auth, HttpServletRequest request) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // Resolve CSRF token safely in Java (avoids DeferredCsrfToken issues in Thymeleaf Spring Security 6)
        try {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken == null) {
                Object raw = request.getAttribute("_csrf");
                if (raw instanceof CsrfToken ct) {
                    csrfToken = ct;
                }
            }
            model.addAttribute("csrfHeader", csrfToken != null ? csrfToken.getHeaderName() : "X-CSRF-TOKEN");
            model.addAttribute("csrfTokenValue", csrfToken != null ? csrfToken.getToken() : "");
        } catch (Exception e) {
            model.addAttribute("csrfHeader", "X-CSRF-TOKEN");
            model.addAttribute("csrfTokenValue", "");
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
        return "perfil-organizador";
    }
}
