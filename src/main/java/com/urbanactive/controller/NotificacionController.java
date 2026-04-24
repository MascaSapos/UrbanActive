package com.urbanactive.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Map<String, Object>> pendientes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("pendiente", false, "mensajes", List.of()));
        }

        String rol = userDetails.getUsuario().getRol().toUpperCase();
        String userId = userDetails.getUsuario().getId();
        boolean pendiente = false;
        List<String> mensajes = new java.util.ArrayList<>();

        if ("ORGANIZADOR".equals(rol) || "ROLE_ORGANIZADOR".equals(rol)) {
            List<Actividad> misActividades = actividadRepository.findByOrganizador_Id(userId);
            for (Actividad a : misActividades) {
                String alerta = actividadService.obtenerMensajeAlerta(a);
                if (alerta != null) {
                    // El organizador solo ve avisos de clima o aforo (no de "No disponible" por cancelación propia)
                    if (alerta.contains("Previsión") || alerta.contains("adversas") || alerta.contains("completo")) {
                        mensajes.add(alerta + " en '" + a.getTipoDeporte() + "'");
                        pendiente = true;
                    }
                }
            }
        } else {
            // Deportista / Usuario corriente


            // 2. Alertas sincronizadas en sus reservas activas
            List<Reserva> misReservas = reservaRepository.findByUsuario(userDetails.getUsuario());
            for (Reserva r : misReservas) {
                // Notificar si está confirmada y hay alerta, O si está cancelada pero la actividad también lo está (aviso de cancelación)
                boolean esCancelacion = "CANCELADA".equalsIgnoreCase(r.getEstado()) && "CANCELADA".equalsIgnoreCase(r.getActividad().getEstado());
                if ("CONFIRMADA".equalsIgnoreCase(r.getEstado()) || esCancelacion) {
                    String alerta = actividadService.obtenerMensajeAlerta(r.getActividad());
                    if (alerta != null) {
                        // Para el deportista, "Actividad no disponible" es un aviso de cancelación
                        mensajes.add(alerta + " para '" + r.getActividad().getTipoDeporte() + "'");
                        pendiente = true;
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of("pendiente", pendiente, "mensajes", mensajes));
    }
}
