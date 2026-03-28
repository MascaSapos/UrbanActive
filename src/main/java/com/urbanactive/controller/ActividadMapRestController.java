package com.urbanactive.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbanactive.dto.ActividadMapDto;
import com.urbanactive.service.ActividadService;

@RestController
@RequestMapping("/api/actividades")
public class ActividadMapRestController {

    private final ActividadService actividadService;

    public ActividadMapRestController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping("/map")
    public List<ActividadMapDto> actividadesParaMapa() {
        return actividadService.obtenerParaMapa();
    }
}
