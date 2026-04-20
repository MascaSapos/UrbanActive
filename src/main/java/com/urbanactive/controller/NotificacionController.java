package com.urbanactive.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbanactive.dto.ActividadMapDto;
import com.urbanactive.model.Actividad;
import com.urbanactive.model.Reserva;
import com.urbanactive.repository.ActividadRepository;
import com.urbanactive.repository.ReservaRepository;
import com.urbanactive.security.CustomUserDetails;
import com.urbanactive.service.ActividadService;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final ReservaRepository reservaRepository;
    private final ActividadRepository actividadRepository;
    private final ActividadService actividadService;

    public NotificacionController(ReservaRepository reservaRepository,
                                  ActividadRepository actividadRepository,
                                  ActividadService actividadService) {
        this.reservaRepository = reservaRepository;
        this.actividadRepository = actividadRepository;
        this.actividadService = actividadService;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<Map<String, Boolean>> pendientes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("pendiente", false));
        }

        String rol = userDetails.getUsuario().getRol().toUpperCase();
        String userId = userDetails.getUsuario().getId();
        boolean pendiente = false;

        if ("ORGANIZADOR".equals(rol) || "ROLE_ORGANIZADOR".equals(rol)) {
            // Revisión de clima en sus actividades
            List<Actividad> misActividades = actividadRepository.findByOrganizador_Id(userId);
            for (Actividad a : misActividades) {
                if (!"CANCELADA".equals(a.getEstado())) {
                    // 1. Revisión de Aforo Completo
                    int ocupadas = reservaRepository.countActivasPorActividad(a.getId());
                    if (a.getPlazasTotal() != null && ocupadas >= a.getPlazasTotal()) {
                        pendiente = true;
                        break;
                    }

                    // 2. Revisión de clima
                    ActividadMapDto.WeatherDto w = actividadService.aWeatherDto(a);
                    if (w != null && w.getAlerta() != null) {
                        pendiente = true;
                        break;
                    }
                }
            }
        } else {
            // Deportista / Usuario corriente
            // 1. Actividad borrada/cancelada
            long canceladas = reservaRepository.countActividadesCanceladasDeportista(userId);
            if (canceladas > 0) {
                pendiente = true;
            } else {
                // 2. Revisión de clima en sus reservas activas
                List<Reserva> misReservas = reservaRepository.findByUsuario(userDetails.getUsuario());
                for (Reserva r : misReservas) {
                    if ("CONFIRMADA".equals(r.getEstado()) && !"CANCELADA".equals(r.getActividad().getEstado())) {
                        ActividadMapDto.WeatherDto w = actividadService.aWeatherDto(r.getActividad());
                        if (w != null && w.getAlerta() != null) {
                            pendiente = true;
                            break;
                        }
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of("pendiente", pendiente));
    }
}
