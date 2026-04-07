package com.urbanactive.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbanactive.dto.ActividadMapDto;
import com.urbanactive.service.ActividadService;
import com.urbanactive.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/actividades")
public class ActividadMapRestController {

    private final ActividadService actividadService;
    private final ReservaService reservaService;

    public ActividadMapRestController(ActividadService actividadService, ReservaService reservaService) {
        this.actividadService = actividadService;
        this.reservaService = reservaService;
    }

    @GetMapping("/map")
    public List<ActividadMapDto> actividadesParaMapa() {
        return actividadService.obtenerParaMapa();
    }

    @PostMapping("/{actividadId}/reservar")
    public ResponseEntity<Map<String, Object>> reservarAjax(@PathVariable String actividadId, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) {
            response.put("exito", false);
            response.put("mensaje", "No estás autenticado. Debes iniciar sesión como Deportista.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            reservaService.crearParaUsuario(actividadId, auth.getName());
            response.put("exito", true);
            response.put("mensaje", "Reserva de plaza realizada con éxito");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("exito", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("exito", false);
            response.put("mensaje", "Error del servidor: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/testmap")
    public ResponseEntity<List<ActividadMapDto>> testmap() {
        return ResponseEntity.ok(actividadService.obtenerParaMapa());
    }
}
