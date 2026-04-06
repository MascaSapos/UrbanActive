package com.urbanactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.urbanactive.service.ActividadService;

import com.urbanactive.repository.ReservaRepository;
import com.urbanactive.model.Actividad;
import java.util.List;

@Controller
public class WebController {

    private final ActividadService actividadService;
    private final ReservaRepository reservaRepository;

    public WebController(ActividadService actividadService, ReservaRepository reservaRepository) {
        this.actividadService = actividadService;
        this.reservaRepository = reservaRepository;
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
}
