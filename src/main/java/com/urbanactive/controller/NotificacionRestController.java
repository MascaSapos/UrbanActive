package com.urbanactive.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbanactive.model.Reserva;
import com.urbanactive.model.Usuario;
import com.urbanactive.model.Actividad;
import com.urbanactive.service.ActividadService;
import com.urbanactive.service.ReservaService;
import com.urbanactive.service.UsuarioService;

/**
 * REST controller that provides lightweight notification counts
 * so the frontend can show/hide the bell dot on the "Mi Perfil" navbar button.
 *
 * Strategy (localStorage-free, server-side):
 *   - Organizer: count reservations on their activities whose fechaReserva is
 *     more recent than the organizer's last visit (tracked via a simple epoch
 *     timestamp stored in localStorage on the client).
 *     Because we have no server-side last-visit tracking we return the total
 *     reservations on open activities; the client compares with its own last-
 *     seen count stored in localStorage.
 *   - Athlete: count reservations whose estado changed to CANCELADA or whose
 *     linked activity estado changed (closed/full) since last client-side visit.
 *     We expose the full list and let the client diff against what it last saw.
 *
 * In practice we return a simple { hasNew: boolean, count: int } object so the
 * client just needs to store the count it last saw in localStorage.
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionRestController {

    private final ActividadService actividadService;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    public NotificacionRestController(ActividadService actividadService,
                                      ReservaService reservaService,
                                      UsuarioService usuarioService) {
        this.actividadService = actividadService;
        this.reservaService   = reservaService;
        this.usuarioService   = usuarioService;
    }

    /**
     * GET /api/notificaciones/count
     *
     * Returns { count: N } where:
     *   - For ORGANIZADOR: N = total confirmed reservations across their activities.
     *   - For DEPORTISTA / USER: N = total reservations for this user.
     *
     * The frontend stores the last seen count in localStorage. If the new count
     * differs → show the red dot.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count(Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) {
            resp.put("count", 0);
            resp.put("hasNew", false);
            return ResponseEntity.ok(resp);
        }

        try {
            Usuario usuario = usuarioService.obtenerPorEmail(auth.getName());
            boolean isOrg = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ORGANIZADOR"));

            int count;
            if (isOrg) {
                // Count total confirmed reservations on all organizer's activities
                List<Actividad> actividades = actividadService.obtenerPorOrganizador(auth.getName());
                count = actividades.stream().mapToInt(act -> {
                    try { return reservaService.contarReservasPorActividad(act.getId()); }
                    catch (Exception e) { return 0; }
                }).sum();
            } else {
                // Count total reservations the athlete has
                List<Reserva> reservas = reservaService.obtenerPorUsuario(usuario);
                count = reservas.size();
            }

            resp.put("count", count);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("count", 0);
            return ResponseEntity.ok(resp);
        }
    }
}
