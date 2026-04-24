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
import com.urbanactive.service.ActividadService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
public class ReservaRestController {

    private final ReservaService reservaService;
    private final UsuarioService usuarioService;
    private final ActividadService actividadService;

    public ReservaRestController(ReservaService reservaService, UsuarioService usuarioService, ActividadService actividadService) {
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
        this.actividadService = actividadService;
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

            List<Map<String, Object>> result = reservas.stream()
                .filter(r -> !"CANCELADA".equalsIgnoreCase(r.getEstado())) // Sacar de la lista si está anulada
                .map(r -> {
                    Map<String, Object> dto = new HashMap<>();
                    String estadoReserva = r.getEstado();
                    Actividad act = r.getActividad();
                    
                    // Si la actividad está cancelada por el organizador, la reserva se considera cancelada
                    // Pero aquí solo filtramos las que el deportista ha anulado voluntariamente
                    if (act != null && "CANCELADA".equalsIgnoreCase(act.getEstado())) {
                        estadoReserva = "CANCELADA";
                    }
                    
                    dto.put("id", r.getId());
                    dto.put("estado", estadoReserva);
                    dto.put("fechaReserva", r.getFechaReserva() != null ? r.getFechaReserva().toString() : null);

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
                        
                        // Alerta sincronizada
                        String alerta = actividadService.obtenerMensajeAlerta(act);
                        actDto.put("alerta", alerta);
                        
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

    @org.springframework.web.bind.annotation.PostMapping("/{reservaId}/cancelar")
    public ResponseEntity<Map<String, Object>> cancelarReserva(@org.springframework.web.bind.annotation.PathVariable String reservaId, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        try {
            Reserva reserva = reservaService.obtenerPorId(reservaId);
            if (!auth.getName().equals(reserva.getUsuario().getEmail())) {
                response.put("exito", false);
                response.put("mensaje", "No tienes permiso para anular esta reserva.");
                return ResponseEntity.status(403).body(response);
            }

            reservaService.cancelarReserva(reservaId);
            response.put("exito", true);
            response.put("mensaje", "Reserva anulada con éxito. Has liberado tu plaza.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("exito", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al anular la reserva");
            return ResponseEntity.status(500).body(response);
        }
    }
}
