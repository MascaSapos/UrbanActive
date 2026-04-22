package com.urbanactive.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbanactive.model.Reserva;
import com.urbanactive.model.Actividad;
import com.urbanactive.service.ReservaService;
import com.urbanactive.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
public class ReservaRestController {

    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    public ReservaRestController(ReservaService reservaService, UsuarioService usuarioService) {
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    /**
     * GET /api/reservas/mis-reservas
     * Returns all reservations for the currently authenticated user as JSON,
     * including nested activity data for the profile page card rendering.
     */
    @GetMapping("/mis-reservas")
    public ResponseEntity<List<Map<String, Object>>> misReservas(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        try {
            com.urbanactive.model.Usuario usuario = usuarioService.obtenerPorEmail(auth.getName());
            List<Reserva> reservas = reservaService.obtenerPorUsuario(usuario);

            List<Map<String, Object>> result = reservas.stream().map(r -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", r.getId());
                dto.put("estado", r.getEstado());
                dto.put("fechaReserva", r.getFechaReserva() != null ? r.getFechaReserva().toString() : null);

                Actividad act = r.getActividad();
                if (act != null) {
                    Map<String, Object> actDto = new HashMap<>();
                    actDto.put("id", act.getId());
                    actDto.put("tipoDeporte", act.getTipoDeporte());
                    actDto.put("fechaHora", act.getFechaHora() != null ? act.getFechaHora().toString() : null);
                    actDto.put("estado", act.getEstado());
                    actDto.put("plazasTotal", act.getPlazasTotal());
                    if (act.getId_ubicacion() != null) {
                        actDto.put("lugar", act.getId_ubicacion().getNombre());
                    }
                    dto.put("actividad", actDto);
                } else {
                    dto.put("actividad", null);
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
